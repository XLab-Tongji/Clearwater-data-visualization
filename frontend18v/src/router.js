import Vue from 'vue'
import Router from 'vue-router'
import Home from './views/Home.vue'

Vue.use(Router)

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [{
      path: '/',
      name: 'main',
      component: () => import( /* webpackChunkName: "about" */ './views/MainPage.vue'),
    },
    {
      path: '/main',
      name: 'mainpage',
      // route level code-splitting
      // this generates a separate chunk (about.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import( /* webpackChunkName: "about" */ './views/MainPage.vue'),
      children: [{
          path: 'series',
          name: 'series',
          component: () => import('./views/SeriesView.vue'),
        },
        {
          path: 'modify',
          name: 'ModiData',
          component: () => import('./views/ModifyView.vue'),
        },
        {
          path: 'graph',
          name: 'ERChara',
          component: () => import('./views/GraphView.vue'),
        }
      ]
    }
  ]
})