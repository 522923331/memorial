<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="留言人" prop="authorName">
        <el-input
          v-model="queryParams.authorName"
          placeholder="请输入留言人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="审核状态" prop="auditStatus">
        <el-select v-model="queryParams.auditStatus" placeholder="审核状态" clearable>
          <el-option
            v-for="dict in dict.type.memorial_audit_status"
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
          type="success"
          plain
          icon="el-icon-check"
          size="mini"
          :disabled="multiple"
          @click="handleBatchAudit"
          v-hasPermi="['memorial:message:audit']"
        >批量审核</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['memorial:message:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['memorial:message:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="messageList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="messageId" width="80" />
      <el-table-column label="逝者ID" align="center" prop="deceasedId" width="100" />
      <el-table-column label="留言内容" align="center" prop="content" :show-overflow-tooltip="true" />
      <el-table-column label="留言人" align="center" prop="authorName" width="120" />
      <el-table-column label="审核状态" align="center" prop="auditStatus" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.memorial_audit_status" :value="scope.row.auditStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-check"
            @click="handleAudit(scope.row)"
            v-hasPermi="['memorial:message:audit']"
          >审核</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['memorial:message:remove']"
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

    <!-- 审核对话框 -->
    <el-dialog title="留言审核" :visible.sync="auditOpen" width="500px" append-to-body>
      <el-form ref="auditForm" :model="auditForm" :rules="auditRules" label-width="80px">
        <el-form-item label="留言内容" v-if="auditForm.content">
          <el-input type="textarea" v-model="auditForm.content" :rows="4" readonly />
        </el-form-item>
        <el-form-item label="审核状态" prop="auditStatus">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio
              v-for="dict in dict.type.memorial_audit_status"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitAudit">确 定</el-button>
        <el-button @click="auditOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMessage, auditMessage, batchAuditMessage, delMessage } from "@/api/memorial/message"

export default {
  name: "Message",
  dicts: ['memorial_audit_status'],
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      messageList: [],
      auditOpen: false,
      auditForm: {},
      auditRules: {
        auditStatus: [
          { required: true, message: "请选择审核状态", trigger: "change" }
        ]
      },
      isBatchAudit: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        authorName: undefined,
        auditStatus: undefined,
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
      listMessage(this.queryParams).then(response => {
        this.messageList = response.rows
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
      this.ids = selection.map(item => item.messageId)
      this.multiple = !selection.length
    },
    handleAudit(row) {
      this.isBatchAudit = false
      this.auditForm = {
        messageId: row.messageId,
        content: row.content,
        auditStatus: undefined
      }
      this.auditOpen = true
      this.$nextTick(() => {
        this.$refs["auditForm"] && this.$refs["auditForm"].clearValidate()
      })
    },
    handleBatchAudit() {
      this.isBatchAudit = true
      this.auditForm = {
        content: undefined,
        auditStatus: undefined
      }
      this.auditOpen = true
      this.$nextTick(() => {
        this.$refs["auditForm"] && this.$refs["auditForm"].clearValidate()
      })
    },
    submitAudit() {
      this.$refs["auditForm"].validate(valid => {
        if (valid) {
          if (this.isBatchAudit) {
            batchAuditMessage({ messageIds: this.ids, auditStatus: this.auditForm.auditStatus }).then(response => {
              this.$modal.msgSuccess("批量审核成功")
              this.auditOpen = false
              this.getList()
            })
          } else {
            auditMessage({ messageId: this.auditForm.messageId, auditStatus: this.auditForm.auditStatus }).then(response => {
              this.$modal.msgSuccess("审核成功")
              this.auditOpen = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const messageIds = row.messageId || this.ids
      this.$modal.confirm('是否确认删除留言编号为"' + messageIds + '"的数据项？').then(function() {
        return delMessage(messageIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('memorial/message/export', {
        ...this.queryParams
      }, `message_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
