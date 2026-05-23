"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Send } from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Textarea } from "@/components/ui/input";
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
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      text: "",
      occurredAt: new Date().toISOString().slice(0, 16),
      locationName: "",
      eventType: "EVERYDAY"
    }
  });

  return (
    <Card>
      <CardContent>
        <form
          className="grid gap-4"
          onSubmit={form.handleSubmit(async (values) => {
            await createPost.mutateAsync({
              ...values,
              occurredAt: new Date(values.occurredAt).toISOString(),
              taggedMemberIds: []
            });
            form.reset({ ...form.getValues(), text: "", locationName: "" });
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
          {form.formState.errors.text ? <p className="font-bold text-[#C15A4A]">{form.formState.errors.text.message}</p> : null}
          <div className="flex justify-end">
            <Button type="submit" disabled={createPost.isPending}>
              <Send className="h-5 w-5" /> {createPost.isPending ? "Sharing..." : "Share memory"}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
}
