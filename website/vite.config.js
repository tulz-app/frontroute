import {resolve} from 'path'
import {createHtmlPlugin} from 'vite-plugin-html'
import tailwindcss from '@tailwindcss/vite'
import viteCompression from 'vite-plugin-compression2';
import fs from 'fs'

import scalaVersion from './scala-version'

const frontrouteVersion = fs.readFileSync('.frontroute-version')

// https://vitejs.dev/config/
export default ({mode}) => {
  const mainJS = `target/scala-${scalaVersion}/website-${mode === 'production' ? 'opt' : 'fastopt'}/main.js`
  const script = `<script type="module" src="/${mainJS}"></script>`

  /** @type {import('vite').UserConfig} */
  return {
    server: {
      port: 6080,
    },
    base: `/v/${frontrouteVersion}/`,
    publicDir: './src/main/public',
    build: {
      outDir: `dist/v/${frontrouteVersion}`,
    },
    plugins: [
      ...(mode === 'production' ? [
        viteCompression({
          include: /\.(js|css|html)$/i,
          algorithms: [
            'gzip',
            'brotliCompress'
          ]
        })
      ] : []),
      tailwindcss(),
      createHtmlPlugin({
        minify: mode === 'production',
        inject: {
          data: {
            script,
            pl: mode === 'production' ? '<script async defer data-domain="frontroute.dev" src="/js/pl-script.js"></script>' : ''
          },
        },
      }),
    ],
    resolve: {
      alias: {
        'stylesheets': resolve(__dirname, './src/main/static/stylesheets'),
        'svg': resolve(__dirname, './src/main/static/svg'),
      }
    }
  }
}
