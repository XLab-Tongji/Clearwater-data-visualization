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
            redirect: '/import',
            name: 'mainpage',
            // route level code-splitting
            // this generates a separate chunk (about.[hash].js) for this route
            // which is lazy-loaded when the route is visited.
            component: () => import( /* webpackChunkName: "about" */ './views/MainPage.vue'),
            children: [
                {
                    path: 'test',
                    name: 'test',
                    component: () => import('./views/dygraph/dy.vue'),
                },
                {
                    path: 'import',
                    name: 'import',
                    component: () => import('./views/import/Import.vue'),
                },
                {
                    path: 'show',
                    name: 'show',
                    component: () => import('./views/showData/test.vue'),
                },
                {
                    path: 'overview',
                    name: 'SystemOverview',
                    component: () => import('./views/KnowledgeGraph/SystemOverview.vue'),
                    // meta:{
                    //     keepAlive:true
                    // }
                },
                // {
                //     path: 'service',
                //     name: 'ServiceCall',
                //     component: () => import('./views/KnowledgeGraph/ServiceCall.vue'),
                // },
                // {
                //     path: 'timestamp',
                //     name: 'EventTimeStamp',
                //     component: () => import('./views/KnowledgeGraph/EventTimeStamp.vue'),
                // }
            ]
        }
    ]
})