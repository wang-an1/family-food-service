alter table `dish`
  add index `idx_dish_family_deleted_status_updated` (`family_id`, `deleted`, `status`, `updated_at` desc, `id` desc);

alter table `dish_tag`
  add index `idx_dish_tag_family_id_cover` (`family_id`, `id`, `name`, `color`);
