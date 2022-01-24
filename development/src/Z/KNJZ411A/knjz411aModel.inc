<?php

require_once('for_php7.php');

//定数定義
class knjz411aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $school_cd;
    public $campus_addr_cd;
    //メンバ変数の初期化
    public function knjz411aModel()
    {
    }

    public function init()
    {
        $this->field = array("tmp_SCHOOL_CD"      =>  VARS::post("tmp_SCHOOL_CD"),
                             "SCHOOL_NAME"        =>  VARS::post("SCHOOL_NAME"),  //検索で使う
                             "SCHOOL_GROUP"       =>  VARS::post("SCHOOL_GROUP"), //検索で使う
                             "FACULTYCD"          =>  VARS::post("FACULTYCD"),
                             "FACULTYNAME"        =>  VARS::post("FACULTYNAME"),
                             "FACULTYNAME_SHOW1"  =>  VARS::post("FACULTYNAME_SHOW1"),
                             "FACULTYNAME_SHOW2"  =>  VARS::post("FACULTYNAME_SHOW2"),
                             "CAMPUS_ADDR_CD"     =>  VARS::post("CAMPUS_ADDR_CD"),
                             "CAMPUS_FACULTYCD"   =>  VARS::post("CAMPUS_FACULTYCD"),
                             );

        $this->cmd = VARS::request("cmd");
        if (VARS::request("cmd") == "") {
            $this->school_cd = "";
            $this->facultycd = "";
        }
        if (VARS::request("SCHOOL_CD") != "") {
            $this->school_cd    = VARS::request("SCHOOL_CD");
        }
        if (VARS::get("FACULTYCD") != "") {
            $this->facultycd    = VARS::get("FACULTYCD");
        }

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
        if (trim($this->field["FACULTYCD"]) == "") {
            $this->setWarning("MSG301", "(学部コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->school_cd=="" || $this->facultycd=="") {
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されているかをチェック
            if ($this->field["tmp_SCHOOL_CD"] !=$this->school_cd) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }
            if ($this->field["FACULTYCD"] !=$this->facultycd) {
                $this->setWarning("MSG308", "キー値は変更できません");
                return false;
            }

            //コードのゼロ埋め
            $this->field["FACULTYCD"] = sprintf("%03d", $this->field["FACULTYCD"]);
            //１レコード取得
            $query = knjz411aQuery::getCollegeFacultyMst_update($this->field["tmp_SCHOOL_CD"], $this->field["FACULTYCD"]);
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
            $this->field["FACULTYCD"] = sprintf("%03d", $this->field["FACULTYCD"]);
            if ($this->field["CAMPUS_FACULTYCD"] != "") {
                $this->field["CAMPUS_FACULTYCD"] = sprintf("%02d", $this->field["CAMPUS_FACULTYCD"]);
            }
            //１レコード取得
            $query = knjz411aQuery::getCollegeFacultyMst_update($this->school_cd, $this->field["FACULTYCD"]);
            $db = Query::dbCheckOut();
            $row = $db->getRow($query);
            Query::dbCheckIn($db);
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG918", "(学部コード)");
                return false;
            }
            if (strlen($this->field["FACULTYCD"]) > 3) {
                $this->setWarning("MSG915", "(学部コードは3バイトまでです。)");
                return false;
            } elseif (strlen($this->field["FACULTYNAME"]) > 90) {
                $this->setWarning("MSG915", "(学部名は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["FACULTYNAME_SHOW1"]) > 90) {
                $this->setWarning("MSG915", "(学部略称1は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["FACULTYNAME_SHOW2"]) > 90) {
                $this->setWarning("MSG915", "(学部略称2は90バイトまでです。)");
                return false;
            } elseif (strlen($this->field["CAMPUS_FACULTYCD"]) > 2) {
                $this->setWarning("MSG915", "(校内推薦用学部コードは2バイトまでです。)");
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
            $this->facultycd = $this->field["FACULTYCD"];
        }
    }
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
            $this->school_cd = $this->field["tmp_SCHOOL_CD"];
            $this->facultycd = $this->field["FACULTYCD"];
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
        $result = knjz411aQuery::getDeleteQuery($this);
        if ( !isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->field = array();
        }
    }

    public function &insert()
    {
        $result = knjz411aQuery::getInsertQuery($this);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    //アップデート時の内部メソッド
    public function &update()
    {
        $result = knjz411aQuery::getUpdateQuery($this);
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
