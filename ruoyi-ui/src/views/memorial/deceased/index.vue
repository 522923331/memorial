<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="逝者姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入逝者姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="性别" prop="gender">
        <el-select v-model="queryParams.gender" placeholder="性别" clearable>
          <el-option
            v-for="dict in dict.type.sys_user_sex"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['memorial:deceased:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['memorial:deceased:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['memorial:deceased:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['memorial:deceased:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="deceasedList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="deceasedId" width="80" />
      <el-table-column label="逝者姓名" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="性别" align="center" prop="gender" width="80">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_user_sex" :value="scope.row.gender"/>
        </template>
      </el-table-column>
      <el-table-column label="出生日期" align="center" prop="birthDate" width="120">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.birthDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="逝世日期" align="center" prop="deathDate" width="120">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.deathDate, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="墓位编号" align="center" prop="cemeteryNumber" width="120" />
      <el-table-column label="所属机构" align="center" prop="orgName" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['memorial:deceased:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-picture"
            @click="handleAlbum(scope.row)"
          >相册</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['memorial:deceased:remove']"
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

    <!-- 添加或修改逝者对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="780px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="逝者姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入逝者姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别">
                <el-option
                  v-for="dict in dict.type.sys_user_sex"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker v-model="form.birthDate" value-format="yyyy-MM-dd" type="date" placeholder="选择出生日期" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="逝世日期" prop="deathDate">
              <el-date-picker v-model="form.deathDate" value-format="yyyy-MM-dd" type="date" placeholder="选择逝世日期" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="陵园区域" prop="cemeteryArea">
              <el-input v-model="form.cemeteryArea" placeholder="请输入陵园区域" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="墓位编号" prop="cemeteryNumber">
              <el-input v-model="form.cemeteryNumber" placeholder="请输入墓位编号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属机构" prop="orgId">
              <el-input v-model="form.orgId" placeholder="请输入机构ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="家属用户ID" prop="familyUserId">
              <el-input v-model="form.familyUserId" placeholder="请输入家属用户ID" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="生平简介" prop="bio">
              <editor v-model="form.bio" :min-height="192"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否公开" prop="isPublic">
              <el-radio-group v-model="form.isPublic">
                <el-radio label="0">公开</el-radio>
                <el-radio label="1">不公开</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="允许留言" prop="allowMessage">
              <el-radio-group v-model="form.allowMessage">
                <el-radio label="0">允许</el-radio>
                <el-radio label="1">不允许</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="留言审核" prop="messageAudit">
              <el-radio-group v-model="form.messageAudit">
                <el-radio label="0">不需审核</el-radio>
                <el-radio label="1">需审核</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 相册管理对话框 -->
    <el-dialog title="相册管理" :visible.sync="albumOpen" width="700px" append-to-body>
      <el-upload
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        list-type="picture-card"
        :file-list="albumFileList"
        :before-upload="beforeAlbumUpload"
        :data="{deceasedId: albumDeceasedId}"
        accept=".jpg,.jpeg,.png,.gif"
      >
        <i class="el-icon-plus"></i>
      </el-upload>
      <div style="margin-top: 15px;">
        <el-table :data="albumList" v-loading="albumLoading">
          <el-table-column label="缩略图" width="100" align="center">
            <template slot-scope="scope">
              <el-image
                style="width: 60px; height: 60px"
                :src="scope.row.imageUrl"
                :preview-src-list="[scope.row.imageUrl]"
                fit="cover"
              ></el-image>
            </template>
          </el-table-column>
          <el-table-column label="描述" prop="description" :show-overflow-tooltip="true" />
          <el-table-column label="排序" prop="sortOrder" width="80" align="center" />
          <el-table-column label="上传时间" prop="createTime" width="160" align="center">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="text"
                icon="el-icon-delete"
                @click="handleDeleteAlbum(scope.row)"
              >删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="albumOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listDeceased, getDeceased, addDeceased, updateDeceased, delDeceased, delAlbum } from "@/api/memorial/deceased"

export default {
  name: "Deceased",
  dicts: ['sys_user_sex'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      deceasedList: [],
      title: "",
      open: false,
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        gender: undefined
      },
      form: {},
      rules: {
        name: [
          { required: true, message: "逝者姓名不能为空", trigger: "blur" }
        ],
        gender: [
          { required: true, message: "性别不能为空", trigger: "change" }
        ],
        birthDate: [
          { required: true, message: "出生日期不能为空", trigger: "change" }
        ],
        deathDate: [
          { required: true, message: "逝世日期不能为空", trigger: "change" }
        ],
        familyUserId: [
          { required: true, message: "家属用户ID不能为空", trigger: "blur" }
        ]
      },
      albumOpen: false,
      albumLoading: false,
      albumList: [],
      albumFileList: [],
      albumDeceasedId: undefined,
      uploadUrl: process.env.VUE_APP_BASE_API + "/memorial/deceased/album/upload",
      uploadHeaders: { Authorization: "Bearer " + this.$store.getters.token }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listDeceased(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.deceasedList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        deceasedId: undefined,
        name: undefined,
        gender: undefined,
        birthDate: undefined,
        deathDate: undefined,
        cemeteryArea: undefined,
        cemeteryNumber: undefined,
        orgId: undefined,
        familyUserId: undefined,
        bio: undefined,
        isPublic: '0',
        allowMessage: '0',
        messageAudit: '0'
      }
      this.resetForm("form")
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.deceasedId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加逝者"
    },
    handleUpdate(row) {
      this.reset()
      const deceasedId = row.deceasedId || this.ids
      getDeceased(deceasedId).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改逝者"
      })
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.deceasedId != undefined) {
            updateDeceased(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addDeceased(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    handleDelete(row) {
      const deceasedIds = row.deceasedId || this.ids
      this.$modal.confirm('是否确认删除逝者编号为"' + deceasedIds + '"的数据项？').then(function() {
        return delDeceased(deceasedIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleExport() {
      this.download('memorial/deceased/export', {
        ...this.queryParams
      }, `deceased_${new Date().getTime()}.xlsx`)
    },
    handleAlbum(row) {
      this.albumDeceasedId = row.deceasedId
      this.albumOpen = true
      this.albumLoading = true
      getDeceased(row.deceasedId).then(response => {
        const albums = response.data.albums || []
        this.albumList = albums
        this.albumFileList = albums.map(item => ({
          name: item.description || '',
          url: item.imageUrl
        }))
        this.albumLoading = false
      }).catch(() => {
        this.albumList = []
        this.albumFileList = []
        this.albumLoading = false
      })
    },
    beforeAlbumUpload(file) {
      const isImage = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
      if (!isImage) {
        this.$message.error('只能上传图片文件!')
        return false
      }
      return true
    },
    handleUploadSuccess(response, file, fileList) {
      if (response.code === 200) {
        this.$modal.msgSuccess("上传成功")
        this.handleAlbum({ deceasedId: this.albumDeceasedId })
      } else {
        this.$message.error(response.msg || '上传失败')
      }
    },
    handleUploadError() {
      this.$message.error('上传失败')
    },
    handleDeleteAlbum(row) {
      this.$modal.confirm('是否确认删除该相册照片？').then(function() {
        return delAlbum(row.albumId)
      }).then(() => {
        this.$modal.msgSuccess("删除成功")
        this.handleAlbum({ deceasedId: this.albumDeceasedId })
      }).catch(() => {})
    }
  }
}
</script>
