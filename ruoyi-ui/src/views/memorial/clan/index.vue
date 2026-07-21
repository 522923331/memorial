<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="族谱名" prop="clanName">
        <el-input
          v-model="queryParams.clanName"
          placeholder="请输入族谱名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="姓氏" prop="surname">
        <el-input
          v-model="queryParams.surname"
          placeholder="请输入姓氏"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
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
          v-hasPermi="['memorial:clan:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="clanList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="clanId" width="80" />
      <el-table-column label="族谱名" align="center" prop="clanName" :show-overflow-tooltip="true" />
      <el-table-column label="姓氏" align="center" prop="surname" width="80" />
      <el-table-column label="族长ID" align="center" prop="familyUserId" width="90" />
      <el-table-column label="成员数" align="center" prop="memberCount" width="80" />
      <el-table-column label="世代数" align="center" prop="generationCount" width="80" />
      <el-table-column label="公开" align="center" prop="isPublic" width="80">
        <template slot-scope="scope">
          <span v-if="scope.row.isPublic === '0'">公开</span>
          <span v-else style="color: #e6a23c">私密</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <span v-if="scope.row.status === '0'" style="color: #67c23a">正常</span>
          <span v-else style="color: #f56c6c">停用</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-user-solid"
            @click="handleMembers(scope.row)"
            v-hasPermi="['memorial:clan:query']"
          >成员</el-button>
          <el-button
            size="mini"
            type="text"
            :icon="scope.row.status === '0' ? 'el-icon-turn-off' : 'el-icon-open'"
            @click="handleToggleStatus(scope.row)"
            v-hasPermi="['memorial:clan:edit']"
          >{{ scope.row.status === '0' ? '下架' : '恢复' }}</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['memorial:clan:remove']"
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

    <!-- 成员管理对话框 -->
    <el-dialog :title="'成员管理 - ' + currentClanName" :visible.sync="memberOpen" width="900px" append-to-body>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="el-icon-plus"
            size="mini"
            @click="handleAddMember"
            v-hasPermi="['memorial:clan:add']"
          >新增成员</el-button>
        </el-col>
      </el-row>
      <el-table v-loading="memberLoading" :data="memberList">
        <el-table-column label="姓名" align="center" prop="name" width="120" />
        <el-table-column label="性别" align="center" prop="gender" width="80">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.sys_user_sex" :value="scope.row.gender"/>
          </template>
        </el-table-column>
        <el-table-column label="世代" align="center" prop="generation" width="80" />
        <el-table-column label="在世" align="center" prop="isAlive" width="80">
          <template slot-scope="scope">
            <span v-if="scope.row.isAlive === '0'" style="color: #67c23a">在世</span>
            <span v-else style="color: #909399">已故</span>
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
        <el-table-column label="关联纪念馆" align="center" prop="deceasedId" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.deceasedId">{{ scope.row.deceasedId }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleEditMember(scope.row)"
              v-hasPermi="['memorial:clan:edit']"
            >修改</el-button>
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDeleteMember(scope.row)"
              v-hasPermi="['memorial:clan:remove']"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="memberOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 成员新增/修改对话框 -->
    <el-dialog :title="memberTitle" :visible.sync="memberFormOpen" width="600px" append-to-body>
      <el-form ref="memberForm" :model="memberForm" :rules="memberRules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="memberForm.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="memberForm.gender" placeholder="请选择性别">
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
            <el-form-item label="是否在世" prop="isAlive">
              <el-radio-group v-model="memberForm.isAlive">
                <el-radio label="0">在世</el-radio>
                <el-radio label="1">已故</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字号" prop="title">
              <el-input v-model="memberForm.title" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker v-model="memberForm.birthDate" value-format="yyyy-MM-dd" type="date" placeholder="选择出生日期" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="memberForm.isAlive === '1'" label="逝世日期" prop="deathDate">
              <el-date-picker v-model="memberForm.deathDate" value-format="yyyy-MM-dd" type="date" placeholder="选择逝世日期" style="width: 100%"></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="肖像URL" prop="avatar">
              <el-input v-model="memberForm.avatar" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介" prop="bio">
              <el-input v-model="memberForm.bio" type="textarea" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitMemberForm">确 定</el-button>
        <el-button @click="memberFormOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listClan,
  listClanMembers,
  addClanMember,
  updateClanMember,
  delClanMember,
  updateClan,
  delClan
} from "@/api/memorial/clan"

export default {
  name: "Clan",
  dicts: ['sys_user_sex'],
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      clanList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        clanName: undefined,
        surname: undefined,
        status: undefined
      },
      // 成员管理
      memberOpen: false,
      memberLoading: false,
      memberList: [],
      currentClanId: undefined,
      currentClanName: '',
      // 成员表单
      memberFormOpen: false,
      memberTitle: '',
      memberForm: {},
      memberRules: {
        name: [{ required: true, message: "姓名不能为空", trigger: "blur" }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listClan(this.queryParams).then(response => {
        this.clanList = response.rows
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
      this.ids = selection.map(item => item.clanId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleToggleStatus(row) {
      const status = row.status === '0' ? '1' : '0'
      const text = status === '1' ? '下架' : '恢复'
      this.$modal.confirm('确定' + text + '族谱「' + row.clanName + '」？').then(() => {
        return updateClan({ clanId: row.clanId, status })
      }).then(() => {
        this.$modal.msgSuccess(text + '成功')
        this.getList()
      }).catch(() => {})
    },
    handleDelete(row) {
      const clanIds = row.clanId || this.ids
      this.$modal.confirm('是否确认删除族谱编号为"' + clanIds + '"的数据项？').then(function() {
        return delClan(clanIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    // ===== 成员管理 =====
    handleMembers(row) {
      this.currentClanId = row.clanId
      this.currentClanName = row.clanName
      this.memberOpen = true
      this.loadMembers()
    },
    loadMembers() {
      this.memberLoading = true
      listClanMembers(this.currentClanId).then(response => {
        this.memberList = response.data || []
        this.memberLoading = false
      }).catch(() => {
        this.memberLoading = false
      })
    },
    resetMemberForm() {
      this.memberForm = {
        memberId: undefined,
        clanId: this.currentClanId,
        name: undefined,
        gender: '0',
        isAlive: '1',
        birthDate: undefined,
        deathDate: undefined,
        title: undefined,
        avatar: undefined,
        bio: undefined
      }
      this.resetForm("memberForm")
    },
    handleAddMember() {
      this.resetMemberForm()
      this.memberFormOpen = true
      this.memberTitle = '新增成员'
    },
    handleEditMember(row) {
      this.resetMemberForm()
      this.memberForm = { ...row }
      this.memberFormOpen = true
      this.memberTitle = '修改成员'
    },
    submitMemberForm() {
      this.$refs["memberForm"].validate(valid => {
        if (valid) {
          this.memberForm.clanId = this.currentClanId
          if (this.memberForm.memberId != undefined) {
            updateClanMember(this.memberForm).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.memberFormOpen = false
              this.loadMembers()
            })
          } else {
            addClanMember(this.memberForm).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.memberFormOpen = false
              this.loadMembers()
            })
          }
        }
      })
    },
    handleDeleteMember(row) {
      this.$modal.confirm('是否确认删除成员「' + (row.name || '未命名') + '」？相关关系也会删除').then(function() {
        return delClanMember(row.memberId)
      }).then(() => {
        this.$modal.msgSuccess("删除成功")
        this.loadMembers()
      }).catch(() => {})
    }
  }
}
</script>
