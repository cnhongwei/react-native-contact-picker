const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const path = require('node:path');
const { test } = require('node:test');
const vm = require('node:vm');
const ts = require('typescript');

function api(native) {
  let lookups = 0;
  const load = (file, require) => {
    const code = ts.transpileModule(
      readFileSync(path.join(__dirname, '../src', file), 'utf8'),
      { compilerOptions: { module: ts.ModuleKind.CommonJS } }
    ).outputText;
    const module = { exports: {} };
    vm.runInNewContext(code, { module, exports: module.exports, require });
    return module.exports;
  };
  const spec = load('NativeContactPicker.ts', () => ({
    TurboModuleRegistry: {
      getEnforcing(name) {
        assert.equal(name, 'ContactPicker');
        lookups++;
        if (!native) throw new Error('Native module not installed');
        return native;
      },
    },
  }));
  const exports = load('index.tsx', () => spec);
  return { ...exports, lookups: () => lookups };
}

test('导入共享库时不加载未安装的原生模块，调用时明确失败', async () => {
  const picker = api(null);
  assert.equal(picker.lookups(), 0);
  await assert.rejects(picker.pickContactPhone(), /not installed/);
});

test('返回用户选中的号码并保留设备格式', async () => {
  const selected = { name: '测试', phoneNumber: '+66 812 345 678' };
  const picker = api({ pickContactPhone: async () => selected });
  assert.equal(await picker.pickContactPhone(), selected);
});

test('取消返回 null', async () => {
  assert.equal(
    await api({ pickContactPhone: async () => null }).pickContactPhone(),
    null
  );
});

test('保留原生错误代码供应用处理', async () => {
  const error = Object.assign(new Error('Busy'), { code: 'E_PICKER_BUSY' });
  await assert.rejects(
    api({
      pickContactPhone: async () => {
        throw error;
      },
    }).pickContactPhone(),
    (actual) => actual === error
  );
});
