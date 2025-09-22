<template>
    <header style="background-color: rgb(10, 47, 88);">
        <div class="text-logo">{{msg}}</div>
        <ul class="header-operations">
        </ul>
    </header>
</template>

<script>
    export default {
        name: 'db-header',
        data () {
            return {
                msg: '服装仓库管理系统'
            }
        }
    }
</script>

<style>
    header {
        height: 70px;
        position: absolute;
        width: 100%;
        top: 0;
        left: 0;
        padding: 0 20px;
        z-index: 1;
        box-sizing: border-box;
    }

    .text-logo {
        display: inline-block;
        vertical-align: middle;
        position: relative;
        top: 26px;
        right: -20px;
        font-size: 25px;
        color: white;
    }

</style>