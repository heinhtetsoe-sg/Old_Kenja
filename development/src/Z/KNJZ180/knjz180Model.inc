<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjz180Model extends Model
{
    public $field      = array();
    public $cmd;
    public $knjz180cd;        //primary
    public $org_knjz180cd;
    public $isload;
    public $year_select;

    /**
     * メンバ変数の初期化
     */
    public function knjz180Model()
    {
        $this->knjz180cd = "";
        $this->org_knjz180cd = "";
        $this->year_select = "";
    }
    public function init()
    {
        $this->field = array("HOLIDAY"            =>  VARS::post("HOLIDAY"),             //日付
                            "REMARK"              =>  VARS::post("REMARK"),              //備考
                            "UPDATED"             =>  VARS::post("UPDATED"),             //更新日付
                            "temp_cd"             =>  VARS::post("temp_cd"),             //最初のCD
                            );

        $this->cmd         = VARS::request("cmd");

        if (VARS::post("HOLIDAY")=="") {
            $this->knjz180cd  = VARS::request("HOLIDAY");
        } else {
            $this->knjz180cd  = VARS::post("HOLIDAY");
        }
        if (VARS::request("year_code") !="") {
            $this->year_code = VARS::request("year_code");
        } else {
            $this->year_code = "";
        }

        if (VARS::get("PROGRAMID")) {
            $this->year_select = CTRL_YEAR;
        }
        if (VARS::request("year_select")) {
            $this->year_select = VARS::request("year_select");
        }

        $this->org_knjz180cd = VARS::request("HOLIDAY");

        if (VARS::post("org_knjz180cd")) {
            $this->org_knjz180cd = VARS::post("org_knjz180cd");
        }
        if ($this->cmd=="clear") {
            $this->knjz180cd = $this->org_knjz180cd;
        }
    }
    //入力項目チェック
    public function validateInput($kind)
    {
        //日付が不正かどうかをチェック
        if ($this->field["HOLIDAY"]=="") {
            if ($kind=="insert") {
                $this->setWarning("MSG902");
            } else {
                $this->setWarning("MSG308");
            }
            $this->knjz180cd = $this->org_knjz180cd;
            return false;
        }
        //１レコード取得
        $row = knjz180Query::getRow($this->knjz180cd);
        $org_row = knjz180Query::getRow($this->org_knjz180cd);
        $this->isload = "0";
        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->field["temp_cd"]=="") {
                $this->isload = "1";
                $this->setWarning("MSG308");
                $this->knjz180cd = $this->org_knjz180cd;
                return false;
            }
            //更新日付チェック1
            if ($this->cmd == "update" && $org_row["UPDATED"] == "") {
                $this->setWarning("MSG303");
                return false;
            }
            //更新日付チェック2
            if ($this->cmd == "update" && $org_row["UPDATED"] != $this->field["UPDATED"]) {
                $this->setWarning("MSG307");
                return false;
            }
        }
        if ($kind=="update" || $kind=="insert") {
            if (trim($this->field["HOLIDAY"]) == "") {
                $this->setWarning("MSG902");
                return false;
            }

            //重複チェック(insert)
            if ($this->cmd == "add" && is_array($row)) {
                $this->setWarning("MSG302");
                return false;
            }
            //重複チェック(update)
            if ($this->cmd == "update" && is_array($row) && $this->knjz180cd != strtr($this->org_knjz180cd, "-", "/")) {
                $this->setWarning("MSG302");
                return false;
            }
            if (strlen($this->field["REMARK"]) >50) {
                $this->setWarning("MSG914", "(備考は50バイトまでです)");
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
        if (!isset($this->knjz180cd)) {
            $this->setWarning("MSG308");
            return;
        }

        $result = knjz180Query::getDeleteQuery($this->field);

        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        $this->field = array();

        return $result;
    }
    public function &insert()
    {
        $result = knjz180Query::getInsertQuery($this->field);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->org_knjz180cd = $this->knjz180cd;
        }
        return $result;
    }

    /**
     * アップデート時の内部メソッド
     */
    public function &update()
    {
        $result = knjz180Query::getUpdateQuery($this->field, $this->org_knjz180cd);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
            $this->org_knjz180cd = $this->knjz180cd;
        }
        return $result;
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
    /**
     * 前年度の休日をコピーする
     */
    public function getCopyModel()
    {
        $db = Query::dbCheckOut();

        if ($db->getOne(knjz180Query::checkCopy($this)) == '0') {
            $this->setMessage("前年度に休日情報がありません。");
            return;
        }
        if ($db->getOne(knjz180Query::checkCopy2($this)) != '0') {
            $this->setMessage("今年度に休日情報が存在します。");
            return;
        }
        knjz180Query::copyQuery($this, $db);
        $this->setMessage("MSG201");

        Query::dbCheckIn($db);
    }
}
