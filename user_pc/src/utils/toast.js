let timer = null
let current = null

export function show(message) {
  if (current) current.remove()
  current = document.createElement('div')
  current.textContent = message
  current.style.cssText = `
    position: fixed; top: 70px; left: 50%; transform: translateX(-50%);
    background: rgba(0,0,0,0.75); color: #fff; padding: 10px 18px;
    border-radius: 6px; font-size: 14px; z-index: 9999; max-width: 80%;
  `
  document.body.appendChild(current)
  clearTimeout(timer)
  timer = setTimeout(() => {
    current && current.remove()
    current = null
  }, 2600)
}