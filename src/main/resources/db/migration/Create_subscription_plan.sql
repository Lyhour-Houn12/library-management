INSERT INTO subscription_plans
(plan_code, name, description, duration_days, price, currency, max_book_allowed, max_days_per_book, display_order, is_active, is_featured, badge_text, admin_notes, created_by, updated_by)
VALUES
    ('MONTHLY_BASIC', 'Basic Monthly', 'Entry-level monthly plan for casual readers', 30, 9.99, 'USD', 3, 14, 1, true, false, NULL, 'Default starter plan', 'system', 'system'),
    ('MONTHLY_STANDARD', 'Standard Monthly', 'Mid-tier monthly plan with more borrowing capacity', 30, 14.99, 'USD', 5, 14, 2, true, false, NULL, NULL, 'system', 'system'),
    ('MONTHLY_PREMIUM', 'Premium Monthly', 'High-tier monthly plan for avid readers', 30, 19.99, 'USD', 10, 21, 3, true, true, 'Most Popular', NULL, 'system', 'system'),
    ('QUARTERLY_BASIC', 'Basic Quarterly', 'Entry-level quarterly plan', 90, 24.99, 'USD', 3, 14, 4, true, false, NULL, NULL, 'system', 'system'),
    ('QUARTERLY_STANDARD', 'Standard Quarterly', 'Mid-tier quarterly plan', 90, 39.99, 'USD', 5, 14, 5, true, false, NULL, NULL, 'system', 'system'),
    ('QUARTERLY_PREMIUM', 'Premium Quarterly', 'High-tier quarterly plan with extended borrowing', 90, 54.99, 'USD', 10, 21, 6, true, true, 'Best Value', NULL, 'system', 'system'),
    ('YEARLY_BASIC', 'Basic Yearly', 'Entry-level annual plan', 365, 89.99, 'USD', 3, 14, 7, true, false, NULL, NULL, 'system', 'system'),
    ('YEARLY_STANDARD', 'Standard Yearly', 'Mid-tier annual plan', 365, 129.99, 'USD', 5, 14, 8, true, true, 'Best Value', NULL, 'system', 'system'),
    ('YEARLY_PREMIUM', 'Premium Yearly', 'Top-tier annual plan with maximum borrowing', 365, 179.99, 'USD', 15, 30, 9, true, true, 'Most Popular', NULL, 'system', 'system'),
    ('WEEKLY_TRIAL', 'Weekly Trial', 'Short trial plan for new users', 7, 2.99, 'USD', 1, 7, 10, true, false, 'Trial', 'Used for onboarding trials', 'system', 'system'),
    ('STUDENT_MONTHLY', 'Student Monthly', 'Discounted monthly plan for verified students', 30, 6.99, 'USD', 3, 14, 11, true, false, 'Student Discount', 'Requires student verification', 'system', 'system'),
    ('STUDENT_YEARLY', 'Student Yearly', 'Discounted annual plan for verified students', 365, 59.99, 'USD', 5, 21, 12, true, false, 'Student Discount', 'Requires student verification', 'system', 'system'),
    ('FAMILY_MONTHLY', 'Family Monthly', 'Shared monthly plan for up to 4 members', 30, 24.99, 'USD', 20, 14, 13, true, true, 'Family Pack', NULL, 'system', 'system'),
    ('FAMILY_YEARLY', 'Family Yearly', 'Shared annual plan for up to 4 members', 365, 249.99, 'USD', 20, 21, 14, true, false, 'Family Pack', NULL, 'system', 'system'),
    ('MONTHLY_EUR', 'Monthly EUR', 'Standard monthly plan billed in Euros', 30, 13.99, 'EUR', 5, 14, 15, true, false, NULL, 'EU region pricing', 'system', 'system'),
    ('YEARLY_EUR', 'Yearly EUR', 'Standard annual plan billed in Euros', 365, 119.99, 'EUR', 5, 14, 16, true, false, NULL, 'EU region pricing', 'system', 'system'),
    ('MONTHLY_GBP', 'Monthly GBP', 'Standard monthly plan billed in Pounds', 30, 11.99, 'GBP', 5, 14, 17, true, false, NULL, 'UK region pricing', 'system', 'system'),
    ('LEGACY_BASIC', 'Legacy Basic (Deprecated)', 'Old basic plan, no longer offered to new users', 30, 7.99, 'USD', 3, 14, 18, false, false, NULL, 'Deprecated - kept for existing subscribers', 'system', 'system'),
    ('LEGACY_PREMIUM', 'Legacy Premium (Deprecated)', 'Old premium plan, no longer offered to new users', 30, 17.99, 'USD', 8, 21, 19, false, false, NULL, 'Deprecated - kept for existing subscribers', 'system', 'system'),
    ('LIFETIME_VIP', 'Lifetime VIP', 'One-time payment for lifetime access', 36500, 499.99, 'USD', 50, 30, 20, true, true, 'VIP', 'Special promotional plan, rarely sold', 'system', 'system');