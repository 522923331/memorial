<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
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
          v-hasPermi="['memorial:statistics:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="statisticsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="statId" width="80" />
      <el-table-column label="逝者ID" align="center" prop="deceasedId" width="100" />
      <el-table-column label="访问次数" align="center" prop="visitCount" width="100" />
      <el-table-column label="留言数" align="center" prop="messageCount" width="100" />
      <el-table-column label="献花数" align="center" prop="flowerCount" width="100" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleDetail(scope.row)"
          >详情</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['memorial:statistics:remove']"
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

    <!-- 统计详情对话框 -->
    <el-dialog title="统计详情" :visible.sync="detailOpen" width="500px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="逝者ID">{{ detailData.deceasedId }}</el-descriptions-item>
        <el-descriptions-item label="访问次数">{{ detailData.visitCount }}</el-descriptions-item>
        <el-descriptions-item label="留言数">{{ detailData.messageCount }}</el-descriptions-item>
        <el-descriptions-item label="献花数">{{ detailData.flowerCount }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listStatistics, getStatisticsByDeceased, delStatistics } from "@/api/memorial/statistics"

export default {
  name: "Statistics",
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      statisticsList: [],
      detailOpen: false,
      detailData: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
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
      listStatistics(this.queryParams).then(response => {
        this.statisticsList = response.rows
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
      this.ids = selection.map(item => item.statId)
      this.multiple = !selection.length
    },
    handleDetail(row) {
      getStatisticsByDeceased(row.deceasedId).then(response => {
        this.detailData = response.data
        this.detailOpen = true
      })
    },
    handleDelete(row) {
      const statIds = row.statId || this.ids
      this.$modal.confirm('是否确认删除统计编号为"' + statIds + '"的数据项？').then(function() {
        return delStatistics(statIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>
