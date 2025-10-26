import { PageInfo } from "./PageInfo";

export interface Page<T> {
    content: T[];
    page: PageInfo;
}