import Vue from 'vue'
import Router from 'vue-router'
import Home from './views/Home.vue'

Vue.use(Router)

export default new Router({
    mode: 'history',
    base: process.env.BASE_URL,
    routes: [
        {
            path: '/',
            name: 'import',
            component: () => import('./views/import/Import.vue'),
            //   component: () => import( /* webpackChunkName: "about" */ './views/MainPage.vue'),
        },
        {
            path: '/show',
            name: 'ShowData',
            component: () => import('./views/showData/Show.vue'),
        },
        {
            path: '/main',
            // redirect: '/overview',
            name: 'mainpage',
            // route level code-splitting
            // this generates a separate chunk (about.[hash].js) for this route
            // which is lazy-loaded when the route is visited.
            component: () => import( /* webpackChunkName: "about" */ './views/MainPage.vue'),
            children: [
                {
                    path: 'class',
                    name: 'ClassGraph',
                    component: () => import('./views/ClassGraph.vue'),
                },
                {
                    path: 'causerel',
                    name: 'CauseRel',
                    component: () => import('./views/CauseRel.vue'),
                },
                {
                    path: 'overview',
                    name: 'SystemOverview',
                    component: () => import('./views/KnowledgeGraph/SystemOverview.vue'),
                    // component: () => import('./views/import/Import.vue'),
                    // component: () => import('./views/import/test.vue'),
                },
                {
                    path: 'service',
                    name: 'ServiceCall',
                    component: () => import('./views/KnowledgeGraph/ServiceCall.vue'),
                },
                {
                    path: 'timestamp',
                    name: 'EventTimeStamp',
                    component: () => import('./views/KnowledgeGraph/EventTimeStamp.vue'),
                }
            ]
        }
    ]
})