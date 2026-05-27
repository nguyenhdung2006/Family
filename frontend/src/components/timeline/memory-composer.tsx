"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { ImagePlus, Send, X } from "lucide-react";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Textarea } from "@/components/ui/input";
import { uploadMedia } from "@/features/albums/api";
import { useCreateTimelinePost } from "@/features/timeline/hooks";

const schema = z.object({
  text: z.string().min(3, "Write a little more.").max(8000),
  occurredAt: z.string().min(1),
  locationName: z.string().max(255).optional(),
  eventType: z.enum(["FAMILY_GATHERING", "BIRTHDAY", "WEDDING", "TET", "TRAVEL", "MEMORIAL", "EVERYDAY", "OTHER"])
});

type FormValues = z.infer<typeof schema>;

export function MemoryComposer() {
  const createPost = useCreateTimelinePost();
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [uploadProgress, setUploadProgress] = useState<number | null>(null);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      text: "",
      occurredAt: new Date().toISOString().slice(0, 16),
      locationName: "",
      eventType: "EVERYDAY"
    }
  });
  const isSaving = createPost.isPending || uploadProgress !== null;

  useEffect(() => {
    return () => {
      if (previewUrl?.startsWith("blob:")) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  function handleFileChange(file?: File) {
    setUploadError(null);
    setSelectedFile(file ?? null);
    setPreviewUrl(file ? URL.createObjectURL(file) : null);
  }

  return (
    <Card>
      <CardContent>
        <form
          className="grid gap-4"
          onSubmit={form.handleSubmit(async (values) => {
            setUploadError(null);
            let uploadedMedia = null;
            if (selectedFile) {
              setUploadProgress(0);
              try {
                uploadedMedia = await uploadMedia(selectedFile, setUploadProgress);
              } catch (error) {
                setUploadError(error instanceof Error ? error.message : "Image upload failed.");
                return;
              } finally {
                setUploadProgress(null);
              }
              if (uploadedMedia.mediaType !== "IMAGE") {
                setUploadError("Timeline memories support image attachments only.");
                return;
              }
            }
            await createPost.mutateAsync({
              ...values,
              occurredAt: new Date(values.occurredAt).toISOString(),
              taggedMemberIds: [],
              mediaUrl: uploadedMedia?.url ?? null,
              mediaStoragePublicId: uploadedMedia?.storagePublicId ?? null,
              mediaType: uploadedMedia?.mediaType ?? null
            });
            form.reset({ ...form.getValues(), text: "", locationName: "" });
            setSelectedFile(null);
            setPreviewUrl(null);
          })}
        >
          <Textarea placeholder="Share a family memory..." {...form.register("text")} />
          <div className="grid gap-3 md:grid-cols-[1fr_1fr_12rem]">
            <Input type="datetime-local" {...form.register("occurredAt")} />
            <Input placeholder="Location" {...form.register("locationName")} />
            <select
              className="min-h-12 rounded-lg border border-border-warm bg-white px-3 text-base font-bold text-ink"
              {...form.register("eventType")}
            >
              {schema.shape.eventType.options.map((event) => (
                <option key={event} value={event}>{event.replace("_", " ")}</option>
              ))}
            </select>
          </div>
          <div className="grid gap-3 rounded-lg border border-dashed border-border-warm bg-surface-soft/50 p-3">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p className="font-black text-ink">Memory photo</p>
                <p className="text-sm font-semibold text-muted">Optional image attachment for the family archive.</p>
              </div>
              <label className="inline-flex min-h-11 cursor-pointer items-center justify-center gap-2 rounded-lg border border-border-warm bg-white px-4 text-base font-bold text-ink transition hover:bg-surface-soft">
                <ImagePlus className="h-5 w-5" />
                Choose image
                <input type="file" accept="image/*" className="sr-only" onChange={(event) => handleFileChange(event.target.files?.[0])} />
              </label>
            </div>
            {previewUrl ? (
              <div className="relative overflow-hidden rounded-lg border border-border-warm bg-white">
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img src={previewUrl} alt="Selected memory attachment preview" className="max-h-72 w-full object-cover" />
                <Button type="button" variant="secondary" size="icon" className="absolute right-2 top-2 bg-white" aria-label="Remove selected image" onClick={() => handleFileChange()}>
                  <X className="h-4 w-4" />
                </Button>
              </div>
            ) : null}
            {uploadProgress !== null ? <p className="font-bold text-muted">Uploading image... {uploadProgress}%</p> : null}
          </div>
          {form.formState.errors.text ? <p className="font-bold text-[#C15A4A]">{form.formState.errors.text.message}</p> : null}
          {uploadError || createPost.error ? <p className="font-bold text-[#C15A4A]">{uploadError ?? createPost.error?.message}</p> : null}
          <div className="flex justify-end">
            <Button type="submit" disabled={isSaving}>
              <Send className="h-5 w-5" /> {isSaving ? "Sharing..." : "Share memory"}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
