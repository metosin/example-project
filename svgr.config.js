module.exports = {
  jsxRuntime: 'automatic',
  icon: true,
  expandProps: false,
  index: false,
  replaceAttrValues: {
    // Replace the color used in the svg files here, so the components
    // can follow current text color.
    '#000': 'currentColor',
  },
  svgoConfig: {
    plugins: [
      {
        name: 'preset-default',
        params: {
          overrides: {
            // Don't remove viewBox attr
            removeViewBox: false,
          }
        }
      }
    ]
  }
}
