# Local Upload Fallback

## Purpose

Digital Family Hub supports Cloudinary uploads for media, but Cloudinary production configuration is not finalized yet.

This DEV-ONLY fallback lets developers upload album media locally when Cloudinary environment variables are missing.

## How Mode Selection Works

The backend checks:

- `hometree.storage.cloudinary-cloud-name`
- `hometree.storage.cloudinary-upload-preset`

If both values are present:

- Cloudinary mode is used.
- Existing upload behavior is preserved.
- Delete remains a no-op for Cloudinary assets until production deletion policy is decided.

If either value is missing and local fallback is enabled outside the `prod` profile:

- Local DEV fallback mode is used.
- Files are stored under the configured local folder.
- The backend serves those files from a safe static resource path.

If the `prod` profile is active:

- Local fallback is disabled by default.
- Missing Cloudinary config should not silently become production filesystem storage.

## Default Local Settings

Configured in `application.yaml`:

```yaml
hometree:
  storage:
    local-fallback-enabled: true
    local-directory: uploads
    local-base-url: http://localhost:8080
    local-public-path: /local-media/
```

Default local file location:

```text
uploads/
```

Default served URL shape:

```text
http://localhost:8080/local-media/{generated-file-name}
```

The folder is auto-created on first upload.

## DEV ONLY Warning

This fallback is for local development only.

Do not use it as production storage because:

- It does not provide durable backup.
- It does not scale across multiple servers.
- It does not provide CDN behavior.
- It does not define retention or restore policy.
- It does not replace a real production media deletion policy.

Production should use a dedicated storage provider such as Cloudinary once policy and credentials are finalized.

## Safety Behavior

Upload safety:

- Only image and video content types are accepted.
- Empty files are rejected.
- Files larger than configured image/video limits are rejected.
- Filenames are generated with UUIDs.
- Original filenames are not trusted as storage paths.
- Files are resolved under the configured local storage root.

Serving safety:

- Only the configured public path is exposed.
- The backend does not expose arbitrary filesystem paths.
- Static serving is only registered when local fallback mode is active.

Delete safety:

- Local delete only accepts the generated storage id filename.
- Path traversal attempts are ignored.
- Delete only targets files inside the configured local storage root.

## How Cloudinary Overrides Fallback

Set both environment variables:

```powershell
$env:CLOUDINARY_CLOUD_NAME="your-cloud-name"
$env:CLOUDINARY_UPLOAD_PRESET="your-upload-preset"
```

When both are non-empty, backend logs should indicate Cloudinary mode.

## Cleanup Expectations

Local files in `uploads/` are development artifacts.

Cleanup options:

- Delete individual media through the app when using local fallback.
- Delete test albums/media from the UI or API.
- Manually remove `uploads/` when resetting local development data.

The folder is ignored by git.

## Manual Verification

1. Make sure Cloudinary environment variables are not set.
2. Start backend in local development mode.
3. Upload an image through Albums.
4. Confirm upload response URL starts with:

```text
http://localhost:8080/local-media/
```

5. Open the URL in a browser.
6. Confirm the image renders.
7. Remove the media from the album.
8. Confirm the local file is removed from `uploads/`.
9. Confirm existing album/media list behavior still works.

## Verification Commands

Backend:

```powershell
.\mvnw.cmd test
```

Frontend:

```powershell
npm run typecheck
.\node_modules\.bin\node.cmd node_modules\eslint\bin\eslint.js . --max-warnings=0
```

