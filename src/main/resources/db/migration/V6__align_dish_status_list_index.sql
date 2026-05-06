alter table `dish`
  add index `idx_dish_family_deleted_updated_id_asc` (`family_id`, `deleted`, `updated_at` desc, `id` asc),
  add index `idx_dish_family_deleted_status_updated_id_asc` (`family_id`, `deleted`, `status`, `updated_at` desc, `id` asc);
