<?php

require_once('for_php7.php');

//定数定義
class knjz412aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $school_cd;
    public $facultycd;
    public $departmentcd;
    public $campus_addr_cd;
    //メンバ変数の初期化
    public function knjz412aModel()
    {
    }

    public function init()
    {
        $this->field = array("tmp_SCHOOL_CD"         =>  VARS::post("tmp_SCHOOL_CD"),
                             "SCHOOL_NAME"           =>  VARS::post("SCHOOL_NAME"),  //検索で使う
                             "SCHOOL_GROUP"          =>  VARS::post("SCHOOL_GROUP"), //検索で使う
                             "tmp_FACULTYCD"         =>  VARS::post("tmp_FACULTYCD"),
                             "DEPARTMENTCD"          =>  VARS::post("DEPARTMENTCD"),
                             "DEPARTMENTNAME"        =>  VARS::post("DEPARTMENTNAME"),
                             "DEPARTMENTNAME_SHOW1"  =>  VARS::post("DEPARTMENTNAME_SHOW1"),
                             "DEPARTMENTNAME_SHOW2"  =>  VARS::post("DEPARTMENTNAME_SHOW2"),
                             "CAMPUS_DEPARTMENTCD"   =>  VARS::post("CAMPUS_DEPARTMENTCD")
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::request("cmd") == "") {
            $this->school_cd = "";
            $this->facultycd = "";
            $this->departmentcd = "";
        }
        if (VARS::request("SCHOOL_CD") != "") {
            $this->school_cd    = VARS::request("SCHOOL_CD");
        }
        if (VARS::request("FACULTYCD") != "") {
            $this->facultycd    = VARS::request("FACULTYCD");
        }
        if (VARS::get("DEPARTMENTCD") != "") {
            $this->departmentcd    = VARS::get("DEPARTMENTCD");
        }
//echo 'school_cd = ' . $this->school_cd;echo "<hr>";
//echo 'facultycd = ' . $this->facultycd;echo "<hr>";
//echo 'departmentcd = ' . $this->departmentcd;echo "<hr>";

        //プロパティファイル
        $this->getPropertiesAll();
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if (trim($this->school_cd) == "") {
            $this->setWarning("MSG301", "(学校コード)");
            return false;
        }
        if (trim($this->facultycd) == "") {
            $this->setWarning("MSG301", "(学部コード)");
            return false;
        }
        if (trim($this->field["DEPARTMENTCD"]) == "") {
            $this->setWarning("MSG301", "(学科コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->school_cd=="" || $this->facultycd=="" || $this->departmentcd=="") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されているかをチェック
            if ($this->field["tmp_SCHOOL_CD"] != $this->school_cd) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
            if ($this->field["tmp_FACULTYCD"] != $this->facultycd) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
            if ($this->field["DEPARTMENTCD"] != $this->departmentcd) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }

            //コードのゼロ埋め
            $this->field["DEPARTMENTCD"] = sprintf("%03d", $this->field["DEPARTMENTCD"]);
            //１レコード取得
            $query = knjz412aQuery::getCollegeDepartmentMst_update($this->field["tmp_SCHOOL_CD"], $this->field["tmp_FACULTYCD"], $this->field["DEPARTMENTCD"]);
            $db = Query::dbCheckOut();
            $row = $db->getRow($query);
            Query::dbCheckIn($db);

            //存在チェック
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {
            //コードのゼロ埋め
            $this->field["DEPARTMENTCD"] = sprintf("%03d", $this->field["DEPARTMENTCD"]);
            if ($this->field["CAMPUS_DEPARTMENTCD"] != "") {
                $this->field["CAMPUS_DEPARTMENTCD"] = sprintf("%02d", $this->field["CAMPUS_DEPARTMENTCD"]);
            }
            //１レコード取得
            $query = knjz412aQuery::getCollegeDepartmentMst_update($this->school_cd, $this->facultycd, $this->field["DEPARTMENTCD"]);
            $db = Query::dbCheckOut();
            $row = $db->getRow($query);
            Query::dbCheckIn($db);
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918", "(学科コード)");
                return false;
            }
            if (strlen($this->field["DEPARTMENTCD"]) > 3) {
                $this->setWarning("MSG915", "(学科コードは3バイトまでです。)");
                return false;
            } elseif (strlen($this->field["DEPARTMENTNAME"]) > 90) {
                $this->setWarning("MSG915", "(学科名は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["DEPARTMENTNAME_SHOW1"]) > 90) {
                $this->setWarning("MSG915", "(学科略称1は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["DEPARTMENTNAME_SHOW2"]) > 90) {
                $this->setWarning("MSG915", "(学科略称2は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["CAMPUS_DEPARTMENTCD"]) > 2) {
                $this->setWarning("MSG915", "(校内推薦用学科コードは2バイトまでです。)");
                return false;
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
            $this->school_cd = $this->field["tmp_SCHOOL_CD"];
            $this->facultycd = $this->field["tmp_FACULTYCD"];
            $this->departmentcd = $this->field["DEPARTMENTCD"];
        }
    }
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
            $this->departmentcd = $this->field["DEPARTMENTCD"];
        }
        return;
    }

    //削除用のモデルを返します
    public function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }
    public function &delete()
    {
        $result = knjz412aQuery::getDeleteQuery($this);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }

    public function &insert()
    {
        $result = knjz412aQuery::getInsertQuery($this);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjz412aQuery::getUpdateQuery($this);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
    //変数をクリアします
    public function clean()
    {
    }
}
