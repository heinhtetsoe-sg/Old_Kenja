<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knjlz001Model extends Model
{
    public $field = array();
    public $cmd;
    public $setting_cd; //primary
    public $seq;        //primary
    public $isload;
    public $SETTING_CD;
    public $SEQ;
    public $CDMEMO;

    /**
     * メンバ変数の初期化
     */
    public function knjlz001Model()
    {
    }
    public function init()
    {
        $this->field = array("ENTEXAMYEAR"      =>  VARS::request("ENTEXAMYEAR") != "" ? VARS::request("ENTEXAMYEAR") : VARS::post("ENTEXAMYEAR"),    //年度
                             "APPLICANTDIV"     =>  VARS::request("APPLICANTDIV") != "" ? VARS::request("APPLICANTDIV") : VARS::post("APPLICANTDIV"), //制度
                             "SETTING_CD"       =>  VARS::request("SETTING_CD") != "" ? VARS::request("SETTING_CD") : VARS::post("SETTING_CD"),       //名称区分
                             "SEQ"              =>  VARS::request("SEQ") != "" ? VARS::request("SEQ") : VARS::post("SEQ"),                            //名称コード
                             "NAME1"            =>  VARS::post("NAME1"),                 //名称1
                             "NAME2"            =>  VARS::post("NAME2"),                 //名称2
                             "NAME3"            =>  VARS::post("NAME3"),                 //名称3
                             "NAME1MEMO"        =>  VARS::post("NAME1MEMO"),             //名称1説明
                             "NAME2MEMO"        =>  VARS::post("NAME2MEMO"),             //名称2説明
                             "NAME3MEMO"        =>  VARS::post("NAME3MEMO"),             //名称3説明
                             "ABBV1"            =>  VARS::post("ABBV1"),                 //略称1
                             "ABBV2"            =>  VARS::post("ABBV2"),                 //略称2
                             "ABBV3"            =>  VARS::post("ABBV3"),                 //略称3
                             "ABBV1MEMO"        =>  VARS::post("ABBV1MEMO"),             //略称1説明
                             "ABBV2MEMO"        =>  VARS::post("ABBV2MEMO"),             //略称2説明
                             "ABBV3MEMO"        =>  VARS::post("ABBV3MEMO"),             //略称3説明
                             "NAMESPARE1"       =>  VARS::post("NAMESPARE1"),            //名称予備1
                             "NAMESPARE2"       =>  VARS::post("NAMESPARE2"),            //名称予備2
                             "NAMESPARE3"       =>  VARS::post("NAMESPARE3"),            //名称予備3
                             "NAMESPARE1MEMO"   =>  VARS::post("NAMESPARE1MEMO"),        //名称予備1説明
                             "NAMESPARE2MEMO"   =>  VARS::post("NAMESPARE2MEMO"),        //名称予備2説明
                             "NAMESPARE3MEMO"   =>  VARS::post("NAMESPARE3MEMO"),        //名称予備3説明
                             "CDMEMO"           =>  VARS::post("CDMEMO"),                //区分説明
                             "MODIFY_FLG"       =>  VARS::post("MODIFY_FLG"),            //学校編集可
                             "temp_cd"          =>  VARS::post("temp_cd"),               //最初のCD
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );

        $this->cmd = VARS::request("cmd");
    }
    //入力項目チェック
    public function validateInput($kind)
    {
        
        //１レコード取得
        $row = knjlz001Query::getRow($this);
        $this->isload = "0";
        if ($kind == "update" || $kind == "delete") {
            //リストより選択していない
            if ($this->field["temp_cd"] == "") {
                $this->isload = "1";
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->field["SETTING_CD"].$this->field["SEQ"] != $this->field["temp_cd"]) {
                $this->isload = "1";
                $this->setWarning("MSG308", "キー値が変更されています。");
                return false;
            }
            //更新日付チェック1
            if ($this->cmd == "update" && $row["UPDATED"] == "") {
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }
        if ($kind == "update" || $kind == "insert") {
            //区分は入力されていない
            if (trim($this->field["SETTING_CD"]) == "") {
                $this->setWarning("MSG301", "(区分)");
                return false;
            }
            //コードは入力されていない
            if (trim($this->field["SEQ"]) == "") {
                $this->setWarning("MSG301", "(コード)");
                return false;
            }
            //コードのゼロ埋め
            if (strlen($this->field["SETTING_CD"]) < 4 && strlen($this->field["SEQ"]) < 4) {
                $this->field["SETTING_CD"] = $this->field["SETTING_CD"] = sprintf("%06d", $this->field["SETTING_CD"]);
                $this->field["SEQ"] = $this->field["SEQ"] = sprintf("%06d", $this->field["SEQ"]);
            }
            //重複チェック
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG302", "(名称コード)");
                return false;
            }
            
            if (strlen($this->field["NAME1"]) > 60) {
                $this->setWarning("MSG915", "(名称は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAME2"]) > 60) {
                $this->setWarning("MSG915", "(名称は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAME3"]) > 60) {
                $this->setWarning("MSG915", "(名称は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAME1MEMO"]) > 60) {
                $this->setWarning("MSG915", "(名称説明は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAME2MEMO"]) > 60) {
                $this->setWarning("MSG915", "(名称説明は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAME3MEMO"]) > 60) {
                $this->setWarning("MSG915", "(名称説明は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ABBV1"]) > 30) {
                $this->setWarning("MSG915", "(略称は30バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ABBV2"]) > 30) {
                $this->setWarning("MSG915", "(略称は30バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ABBV3"]) > 30) {
                $this->setWarning("MSG915", "(略称は30バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ABBV1MEMO"]) > 60) {
                $this->setWarning("MSG915", "(略称説明は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ABBV2MEMO"]) > 60) {
                $this->setWarning("MSG915", "(略称説明は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["ABBV3MEMO"]) > 60) {
                $this->setWarning("MSG915", "(略称説明は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAMESPARE1"]) > 30) {
                $this->setWarning("MSG915", "(予備は30バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAMESPARE2"]) > 30) {
                $this->setWarning("MSG915", "(予備は30バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAMESPARE3"]) > 30) {
                $this->setWarning("MSG915", "(予備は30バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAMESPARE1MEMO"]) > 60) {
                $this->setWarning("MSG915", "(予備説明は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAMESPARE2MEMO"]) > 60) {
                $this->setWarning("MSG915", "(予備説明は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["NAMESPARE3MEMO"]) > 60) {
                $this->setWarning("MSG915", "(予備説明は60バイトまでです。)");
                return false;
            } elseif (strlen($this->field["CDMEMO"]) > 45) {
                $this->setWarning("MSG915", "(区分説明は45バイトまでです。)");
                return false;
            }
        }
        if ($kind == "insert" || $kind == "delete") {
            $row = knjlz001Query::check($this);
            if ($row["MODIFY_FLG"] == "1") {
                $this->setWarning("MSG300", "");
                return false;
            }
        }
        return true;
    }
    /**
     * 編集完了用のモデルを返します
    */
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
        }
        return;
    }

    /**
     * 削除用のモデルを返します
     */
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
        if (!isset($this->field["ENTEXAMYEAR"]) || !isset($this->field["APPLICANTDIV"]) || !isset($this->field["SETTING_CD"]) || !isset($this->field["SEQ"])) {
            $this->setWarning("MSG303");
            return;
        }

        $result = knjlz001Query::getDeleteQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        $setYear = $this->field["ENTEXAMYEAR"];
        $setAppDiv = $this->field["APPLICANTDIV"];
        $setSettingCd = $this->field["SETTING_CD"];
        $this->field = array();
        $this->field["ENTEXAMYEAR"] = $setYear;
        $this->field["APPLICANTDIV"] = $setAppDiv;
        if ($result) {
            $this->field["SETTING_CD"] = $setSettingCd;
        }
        return $result;
    }

    public function &insert()
    {
        $result = knjlz001Query::getInsertQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    public function &update()
    {
        $result = knjlz001Query::getUpdateQuery($this);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    //コピー用のモデルを返します
    public function &getCopyModel()
    {
        knjlz001Query::getCopyQuery($this);
        $this->setMessage("MSG201");
    }
}
