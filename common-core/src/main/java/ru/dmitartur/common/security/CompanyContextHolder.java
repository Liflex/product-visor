package ru.dmitartur.common.security;

public final class CompanyContextHolder {
    private static final ThreadLocal<String> CURRENT_COMPANY_ID = new ThreadLocal<>();

    private CompanyContextHolder() {}

    public static void setCompanyId(String companyId) { CURRENT_COMPANY_ID.set(companyId); }
    public static String getCompanyId() { return CURRENT_COMPANY_ID.get(); }
    public static void clear() { CURRENT_COMPANY_ID.remove(); }
}



