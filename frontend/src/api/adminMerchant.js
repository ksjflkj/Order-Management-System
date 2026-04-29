import request from "./request";

export function getMerchantPage(params) {
    return request({
        url: "/api/admin/merchant/page",
        method: "get",
        params,
    });
}

export function auditMerchant(data) {
    return request({
        url: "/api/admin/merchant/audit",
        method: "post",
        data,
    });
}

export function updateMerchantStatus(data) {
    return request({
        url: "/api/admin/merchant/status",
        method: "post",
        data,
    });
}
