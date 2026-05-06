alter table `dish`
  add index `idx_dish_family_status_deleted_updated_id_asc` (`family_id`, `status`, `deleted`, `updated_at` desc, `id` asc);

alter table `personal_order`
  add index `idx_order_user_family_deleted_created` (`user_id`, `family_id`, `deleted`, `created_at` desc, `id` desc);
