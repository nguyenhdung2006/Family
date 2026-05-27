# Kitchen Categories/Tags Planning

## 1. Mục đích

Tài liệu này là bản thiết kế an toàn cho tính năng danh mục/thẻ công thức trong Kitchen module của Digital Family Hub. Mục tiêu hiện tại chỉ là lập kế hoạch, chưa triển khai code, chưa đổi schema, chưa thêm migration và chưa đổi API contract.

Kitchen hiện đã đủ nền tảng để lưu công thức gia đình, nhưng taxonomy như danh mục, tags, loại món, dịp nấu, vùng miền hoặc chế độ ăn vẫn cần quyết định sản phẩm trước khi làm thật.

## 2. Trạng thái Kitchen hiện tại

### Cấu trúc recipe hiện có

Entity `Recipe` hiện có các trường chính:

- `title`: tên món, bắt buộc.
- `description`: mô tả ngắn, tùy chọn.
- `ingredients`: nguyên liệu, bắt buộc.
- `instructions`: cách làm, bắt buộc.
- `videoUrl`: liên kết video, tùy chọn.
- `notesFromElders`: ghi chú từ người lớn tuổi, tùy chọn.
- `createdBy`: người tạo công thức.

DTO response hiện trả thêm:

- `createdById`
- `createdByName`

Chưa có trường `category`, `tags`, `mealType`, `cuisine`, `occasion` hoặc bảng taxonomy riêng.

### Search hiện tại

Endpoint danh sách recipe hiện hỗ trợ query nhẹ:

```text
GET /api/kitchen/recipes?search=
```

Hành vi hiện tại:

- Không có `search` hoặc `search` rỗng: trả về danh sách như trước.
- Có `search`: tìm trong `title`, `description`, `ingredients`.
- Không dùng full-text engine, Elasticsearch, fuzzy search hoặc migration schema.

### Ownership hiện tại

Quyền mutation hiện dựa trên user đăng nhập:

- `ADMIN`: có thể sửa/xóa mọi recipe.
- `MEMBER`: chỉ sửa/xóa recipe do chính mình tạo.
- `VIEWER`: không được tạo/sửa/xóa.

Backend set `createdBy` từ authenticated user, không tin dữ liệu ownership từ client.

### UI hiện tại

Kitchen page hiện có:

- Danh sách recipe.
- Form tạo recipe.
- Edit/delete recipe theo quyền.
- Search input nhẹ.
- Empty state cho không có recipe và không có kết quả search.

UI chưa có:

- Category dropdown.
- Tag chips.
- Bộ lọc nhiều điều kiện.
- Trang quản lý taxonomy.

## 3. Các hướng tiếp cận tương lai

### A. Simple String Category

Thêm một trường chuỗi đơn giản vào recipe, ví dụ `category`.

Ví dụ dữ liệu:

```text
category = "Món chính"
category = "Món chay"
category = "Tráng miệng"
```

| Tiêu chí | Đánh giá |
| --- | --- |
| Complexity | Thấp. Dễ hiểu, dễ triển khai. |
| Migration impact | Cần thêm một cột nullable nếu làm ở database. Không nên mandatory. |
| Frontend impact | Thêm input/dropdown đơn giản vào form và filter. |
| Search impact | Có thể thêm `?category=` hoặc search kèm category. |
| Scalability | Vừa đủ cho project nhỏ, nhưng dễ bị trùng tên như `Món chính`, `mon chinh`, `Main`. |
| Rollback difficulty | Thấp nếu là nullable field và API vẫn backward-compatible. |

Ưu điểm:

- Phù hợp MVP.
- Dễ test thủ công.
- Ít thay đổi UI.

Nhược điểm:

- Không hỗ trợ nhiều category.
- Không kiểm soát chính tả/tên gọi tốt nếu dùng free text.

### B. Multiple Tags List

Thêm danh sách tags cho mỗi recipe.

Ví dụ dữ liệu:

```text
tags = ["món chay", "nhanh", "bữa sáng"]
tags = ["ngày Tết", "món truyền thống"]
```

| Tiêu chí | Đánh giá |
| --- | --- |
| Complexity | Trung bình. Cần quyết định lưu dạng text array, JSON, hoặc bảng phụ. |
| Migration impact | Có migration nếu thêm cột JSON/text hoặc bảng join. |
| Frontend impact | Cần tag chips, nhập/xóa tag, hiển thị tags trong card. |
| Search impact | Có thể lọc bằng `?tag=` hoặc nhiều `?tag=`. |
| Scalability | Tốt hơn category đơn, nhưng cần chuẩn hóa tag để tránh trùng. |
| Rollback difficulty | Trung bình, phụ thuộc cách lưu. |

Ưu điểm:

- Linh hoạt hơn category.
- Phù hợp cách gia đình nhớ món ăn theo dịp, nguyên liệu, kiểu món.

Nhược điểm:

- UI nhập tags có thể khó hơn cho người lớn tuổi.
- Nếu không chuẩn hóa, dữ liệu tags sẽ bị loạn nhanh.

### C. Separate Normalized Tables

Tạo bảng riêng cho categories/tags, ví dụ:

- `recipe_categories`
- `recipe_tags`
- `recipe_tag_assignments`

| Tiêu chí | Đánh giá |
| --- | --- |
| Complexity | Cao hơn. Cần entity, repository, service, migration, API quản lý taxonomy. |
| Migration impact | Rõ ràng và nhiều hơn các cách khác. |
| Frontend impact | Cần dropdown/autocomplete/chips và có thể cần màn quản lý tags. |
| Search impact | Query có thể chính xác hơn, nhưng phức tạp hơn. |
| Scalability | Tốt nhất nếu app lớn, nhiều recipe, nhiều gia đình, cần quản trị tags. |
| Rollback difficulty | Cao hơn vì có nhiều bảng và quan hệ. |

Ưu điểm:

- Dữ liệu sạch hơn.
- Dễ mở rộng cho admin taxonomy, thống kê, recommendation sau này.

Nhược điểm:

- Quá nặng cho giai đoạn hiện tại.
- Cần quyết định sản phẩm rõ: ai được tạo tag, ai được sửa/xóa tag, tag là toàn app hay theo gia đình.

### D. Hybrid Approach

Bắt đầu bằng category nullable đơn giản, sau đó thêm tags ở dạng nhẹ hoặc normalized khi sản phẩm rõ hơn.

Ví dụ lộ trình:

1. Thêm `category` nullable.
2. UI dùng dropdown danh sách cố định.
3. Sau khi có dữ liệu thật, quyết định có cần tags không.
4. Nếu cần, migrate sang tags/table riêng sau.

| Tiêu chí | Đánh giá |
| --- | --- |
| Complexity | Thấp đến trung bình. Có thể triển khai theo từng bước. |
| Migration impact | Ban đầu nhỏ nếu chỉ thêm nullable field. Tương lai có thể tăng. |
| Frontend impact | Ban đầu nhẹ, sau đó mở rộng dần. |
| Search impact | Có thể thêm `?category=` trước, `?tag=` sau. |
| Scalability | Tốt nếu giữ migration cẩn thận. |
| Rollback difficulty | Thấp ở bước đầu, trung bình nếu thêm tags sau. |

Ưu điểm:

- Thực tế nhất cho project hiện tại.
- Giảm rủi ro over-engineering.
- Cho phép học từ cách người dùng thật phân loại công thức.

Nhược điểm:

- Cần giữ API/DTO có đường tiến hóa rõ.
- Nếu thiết kế category quá cứng, sau này đổi sang tags có thể cần mapping lại dữ liệu.

## 4. Khuyến nghị an toàn cho bước tiếp theo

Khuyến nghị hiện tại: **Hybrid Approach, bắt đầu bằng một `category` nullable đơn giản**, nhưng chỉ triển khai sau khi đã chốt danh sách category ban đầu.

Lý do:

- Project đang ở giai đoạn student/MVP, chưa cần taxonomy phức tạp.
- Kitchen đã có CRUD, ownership và search; category đơn sẽ bổ sung giá trị nhanh mà không làm vỡ kiến trúc.
- Nullable field giúp không phá record cũ.
- UI dropdown dễ dùng hơn tag editor đối với người lớn tuổi.
- Có thể giữ tags là tính năng sau, khi đã biết gia đình thật sự muốn phân loại theo món, dịp, nguyên liệu, vùng miền hay chế độ ăn.

Danh sách category ban đầu nên rất ngắn, ví dụ:

- Món chính
- Món phụ
- Canh/súp
- Tráng miệng
- Đồ uống
- Món dịp lễ
- Khác

Danh sách này cần người dùng/project owner quyết định trước khi code.

## 5. Ý tưởng API không phá vỡ tương thích

Các ý tưởng này chỉ là thiết kế tương lai, chưa triển khai:

```text
GET /api/kitchen/recipes?search=&category=
GET /api/kitchen/recipes?search=&tag=
GET /api/kitchen/recipes?search=&tag=mon-chay&tag=tet
```

DTO evolution nên theo hướng optional:

```json
{
  "id": "...",
  "title": "Bánh chưng",
  "description": "...",
  "ingredients": "...",
  "instructions": "...",
  "category": "Món dịp lễ",
  "tags": ["Tết", "truyền thống"]
}
```

Nguyên tắc API:

- Field mới phải optional/nullable.
- Request cũ không gửi `category`/`tags` vẫn phải hoạt động.
- Response có field mới không được làm hỏng frontend cũ.
- `?category=` rỗng phải tương đương không filter.
- `?tag=` rỗng phải tương đương không filter.
- Không thay đổi behavior của `?search=` hiện tại.

## 6. Ghi chú UI/UX

### Category dropdown

Phù hợp nhất nếu chọn bước đầu là category đơn:

- Dùng dropdown/select đơn giản.
- Có lựa chọn `Khác`.
- Có filter category phía trên danh sách.
- Không bắt buộc chọn category khi tạo/sửa recipe.

### Tags/chips

Chỉ nên làm khi đã quyết định cần nhiều nhãn:

- Hiển thị tag bằng chips nhỏ trên recipe card.
- Cho phép xóa tag rõ ràng trong form edit.
- Tránh để người dùng phải nhớ cú pháp.
- Cân nhắc autocomplete từ tags đã có thay vì nhập tự do hoàn toàn.

### Mobile friendliness

- Filter không nên chiếm quá nhiều chiều cao.
- Search và category filter nên xếp dọc trên mobile.
- Chips phải wrap tốt, không làm vỡ card.

### Elderly-family usability

- Ưu tiên dropdown ngắn hơn tag editor phức tạp.
- Dùng nhãn quen thuộc, tránh thuật ngữ kỹ thuật như taxonomy.
- Không bắt người dùng phân loại trước khi lưu món.
- Cho phép recipe vẫn có giá trị khi không có category/tags.

## 7. Do Not Yet Implement

Không nên triển khai ở giai đoạn kế tiếp nếu chưa có quyết định sản phẩm:

- Advanced taxonomy.
- Nested categories.
- AI tagging.
- Full-text search infrastructure.
- Recommendation engine.
- Tag moderation workflow.
- Global shared taxonomy cho toàn bộ family hubs.
- Import/export taxonomy.
- Analytics theo category/tag.
- Tự động merge tags trùng.

## 8. Ghi chú an toàn migration

Nếu sau này triển khai category/tags, cần giữ nguyên tắc:

- Tránh mandatory columns.
- Field mới nên nullable.
- Preserve old recipe records.
- Không đổi ý nghĩa của các field hiện có.
- Không xóa hoặc rename field đang dùng bởi frontend.
- Không thay đổi ownership/edit/delete rules.
- Không làm query list hiện tại bắt buộc có filter.
- Thêm index chỉ khi có dữ liệu hoặc hiệu năng thật sự cần.
- Migration phải có rollback plan hoặc ít nhất có cách backfill an toàn.

## 9. Đề xuất thứ tự triển khai tương lai

1. Chốt danh sách category ban đầu với project owner.
2. Thiết kế nullable `category` field và DTO optional.
3. Thêm filter `?category=` không phá behavior cũ.
4. Thêm dropdown category vào Kitchen form và filter bar.
5. Test role matrix: ADMIN/MEMBER/VIEWER vẫn giữ đúng quyền.
6. Sau khi có dữ liệu thật, đánh giá có cần tags không.
7. Nếu cần tags, chọn giữa JSON/text nhẹ hoặc normalized tables.

## 10. Câu hỏi cần quyết định trước khi code

- Category là danh sách cố định hay user có thể tự tạo?
- Category dùng chung toàn app hay theo từng gia đình?
- Một recipe có đúng một category hay nhiều category?
- Tags có cần chuẩn hóa chính tả không?
- VIEWER có được lọc/search category không? Khuyến nghị: có, vì đây là read-only.
- ADMIN có quản lý taxonomy riêng không?
- Có cần phân loại theo văn hóa gia đình như món Tết, giỗ, sinh nhật, món quê không?

## 11. Kết luận

Kitchen hiện nên giữ hướng nhẹ: CRUD ổn, ownership rõ, search đơn giản. Tính năng category/tags nên bắt đầu bằng một bước nhỏ, optional và có thể rollback. Với project hiện tại, cách an toàn nhất là chưa làm tags phức tạp, mà chốt category đơn trước rồi quan sát nhu cầu thật.
