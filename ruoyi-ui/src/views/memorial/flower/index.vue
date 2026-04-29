<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="献花人" prop="visitorName">
        <el-input
          v-model="queryParams.visitorName"
          placeholder="请输入献花人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="鲜花类型" prop="flowerType">
        <el-select v-model="queryParams.flowerType" placeholder="鲜花类型" clearable>
          <el-option
            v-for="dict in dict.type.memorial_flower_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="逝者ID" prop="deceasedId">
        <el-input
          v-model="queryParams.deceasedId"
          placeholder="请输入逝者ID"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['memorial:flower:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['memorial:flower:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="flowerList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="flowerId" width="80" />
      <el-table-column label="逝者" align="center" prop="deceasedName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="献花人" align="center" prop="visitorName" width="120" />
      <el-table-column label="鲜花类型" align="center" prop="flowerType" width="120">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.memorial_flower_type" :value="scope.row.flowerType"/>
        </template>
      </el-table-column>
      <el-table-column label="献花留言" align="center" prop="message" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="100">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['memorial:flower:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listFlower, delFlower } from "@/api/memorial/flower"

export default {
  name: "Flower",
  dicts: ['memorial_flower_type'],
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      flowerList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        visitorName: undefined,
        flowerType: undefined,
        deceasedId: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listFlower(this.queryParams).then(response => {
        this.flowerList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.flowerId)
      this.multiple = !selection.length
    },
    handleDelete(row) {
      const flowerIds = row.flowerId || this.ids
      this.$modal.confirm('是否确认删除献花记录编号为"' + flowerIds + '"的数据项？').then(function() {
        return delFlower(flowerIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('memorial/flower/export', {
        ...this.queryParams
      }, `flower_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
