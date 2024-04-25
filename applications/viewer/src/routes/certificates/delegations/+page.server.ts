import type { PageLoad } from './$types'

export const load: PageLoad = async ({params, url}) => {
    let page = url.searchParams.get('page');
    if (!page) page = 0;
    const count = 20;

    const INDEXER_BASE_URL = import.meta.env.VITE_INDEXER_BASE_URL;
    const apiUrl = `${INDEXER_BASE_URL}/stake/delegations?page=${page}&count=${count}`;
    console.log(apiUrl);

    const res = await fetch(apiUrl);
    const data = await res.json();

    const delegations  = data;
    console.log(data);

    if (res.ok) {
        return {
            delegations,
            total: 0,
            total_pages: 0,
            page: page,
            count: count
        }
    }

    return {
        status: 404,
        body: { error: 'Can not fetch stake delegations.' }
    };

}
