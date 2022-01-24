<?php

require_once('for_php7.php');

//定数定義

class knjz251Model extends Model {
    var $field      = array();
    var $cmd;
    var $certif_kindcd = "";        //primary
    var $hasThisYear;

    //メンバ変数の初期化
    function knjz251Model()
    {
    }
    function init()
    {
        $this->hasThisYear = true;

        $this->field = array("CERTIF_KINDCD"   =>  VARS::post("CERTIF_KINDCD"),       //証明書種類コード
                             "KINDNAME"        =>  VARS::post("KINDNAME"),            //証明書種類名
                             "CERTIF_NO"       =>  VARS::post("CERTIF_NO"),           //発行番号
                             "SYOSYO_NAME"     =>  VARS::post("SYOSYO_NAME"),         //証書名
                             "SYOSYO_NAME2"    =>  VARS::post("SYOSYO_NAME2"),        //証書名2
                             "SCHOOL_NAME"     =>  VARS::post("SCHOOL_NAME"),         //学校名
                             "JOB_NAME"        =>  VARS::post("JOB_NAME"),            //責任者職種名
                             "PRINCIPAL_NAME"  =>  VARS::post("PRINCIPAL_NAME"),      //記載責任者名
                             "REMARK1"         =>  VARS::post("REMARK1"),             //備考１
                             "REMARK2"         =>  VARS::post("REMARK2"),             //備考２
                             "REMARK3"         =>  VARS::post("REMARK3"),             //備考３
                             "REMARK4"         =>  VARS::post("REMARK4"),             //備考４
                             "REMARK5"         =>  VARS::post("REMARK5"),             //備考５
                             "REMARK6"         =>  VARS::post("REMARK6"),             //備考６
                             "REMARK7"         =>  VARS::post("REMARK7"),             //備考７
                             "REMARK8"         =>  VARS::post("REMARK8"),             //備考８
                             "REMARK9"         =>  VARS::post("REMARK9"),             //備考９
                             "REMARK10"        =>  VARS::post("REMARK10"),            //備考１０
                             "UPDATED"         =>  VARS::post("UPDATED"),             //更新日付
                            );
        $this->cmd = VARS::request("cmd");
        if (VARS::get("CERTIF_KINDCD")!=""){
            $this->certif_kindcd  = VARS::get("CERTIF_KINDCD");
        }
        if (VARS::get("firstflg") == "on"){
            unset($this->field);
            unset($this->certif_kindcd);
        }
    }
    //入力項目チェック
    function validateInput($kind)
    {
        if (trim($this->field["CERTIF_KINDCD"]) == "") {
            $this->setWarning("MSG301","(証明書種類コード)");
            return false;
        }

        if ($kind=="update" || $kind=="delete") {
            //リストより選択していない
            if ($this->certif_kindcd==""){
                $this->setWarning("MSG308");
                return false;
            }
            //キー値が変更されていますかをチェック
            if ($this->certif_kindcd != $this->field["CERTIF_KINDCD"]){
                $this->setWarning("MSG308");
                return false;
            }

            //コードのゼロ埋め
            $this->field["CERTIF_KINDCD"] = sprintf("%03d",$this->field["CERTIF_KINDCD"]);
        }
        if ($kind=="update" || $kind=="insert"){

            //コードのゼロ埋め
            $this->field["CERTIF_KINDCD"] = sprintf("%03d",$this->field["CERTIF_KINDCD"]);
            //１レコード取得
            $dataflg = knjz251Query::checkCertif($this->field["CERTIF_KINDCD"],CTRL_YEAR);

            //重複チェック
            if ($this->cmd == "add" && $dataflg > 0) {
                $this->setWarning("MSG918","(証明書種類コード)");
                return false;
            }
            if (strlen($this->field["KINDNAME"]) == 0) {
                $this->setWarning("MSG301","(証明書種類名称)");
                return false;
            }
            if (strlen($this->field["KINDNAME"]) > 24) {
                $this->setWarning("MSG915","(証明書種類名称は24バイトまでです)");
                return false;
            }
            if (strlen($this->field["SYOSYO_NAME"]) > 150) {
                $this->setWarning("MSG915","(証書名は90バイトまでです)");
                return false;
            }
            if (strlen($this->field["SYOSYO_NAME2"]) > 30) {
                $this->setWarning("MSG915","(証書名2は30バイトまでです)");
                return false;
            }
            if (strlen($this->field["SCHOOL_NAME"]) > 90) {
                $this->setWarning("MSG915","(学校名は90バイトまでです)");
                return false;
            }
            if (strlen($this->field["JOB_NAME"]) > 135) {
                $this->setWarning("MSG915","(職種名は135バイトまでです)");
                return false;
            }
            if (strlen($this->field["PRINCIPAL_NAME"]) > 90) {
                $this->setWarning("MSG915","(校長名は90バイトまでです)");
                return false;
            }
            for ($i = 1; $i <= 10; $i++) {
                if (strlen($this->field["REMARK".$i]) > 150) {
                    $this->setWarning("MSG915","(備考".$i."は150バイトまでです)");
                    return false;
                }
            }
        }
        return true;
    }

    //追加完了用のモデルを返します
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
            $this->certif_kindcd = $this->field["CERTIF_KINDCD"];
        }
        return;
    }
    //インサート時の内部メソッド
    function &insert()
    {
        knjz251Query::getInsertQuery($this->field);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return;
    }

    //更新完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
        }
    }
    //アップデート時の内部メソッド
    function &update()
    {
        knjz251Query::getUpdateQuery($this->field);
        if (!isset($model->warning)) {
            $this->setMessage("MSG201");
        }
        return;
    }

    //コピー用のモデルを返します
    function &getCopyModel() {
        $put = array();

        $query = knjz251Query::countThisYear(CTRL_YEAR);
        $db = Query::dbCheckOut();
        $result = $db->getOne($query);
        Query::dbCheckIn($db);
        if ($result > 0) {
            $this->hasThisYear = true;
            $this->setMessage("今年度のデータがあります");
        } else {
            $this->hasThisYear = false;
            $query = knjz251Query::getLastYear();
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $put["YEAR"][TEXT]             = CTRL_YEAR;
                $put["CERTIF_KINDCD"][TEXT]    = $row["CERTIF_KINDCD"];
                $put["KINDNAME"][TEXT]         = $row["KINDNAME"];
                $put["CERTIF_NO"][TEXT]        = $row["CERTIF_NO"];
                $put["SYOSYO_NAME"][TEXT]      = $row["SYOSYO_NAME"];
                $put["SYOSYO_NAME2"][TEXT]     = $row["SYOSYO_NAME2"];
                $put["SCHOOL_NAME"][TEXT]      = $row["SCHOOL_NAME"];
                $put["JOB_NAME"][TEXT]         = $row["JOB_NAME"];
                $put["PRINCIPAL_NAME"][TEXT]   = $row["PRINCIPAL_NAME"];
                $put["REMARK1"][TEXT]          = $row["REMARK1"];
                $put["REMARK2"][TEXT]          = $row["REMARK2"];
                $put["REMARK3"][TEXT]          = $row["REMARK3"];
                $put["REMARK4"][TEXT]          = $row["REMARK4"];
                $put["REMARK5"][TEXT]          = $row["REMARK5"];
                $put["REMARK6"][TEXT]          = $row["REMARK6"];
                $put["REMARK7"][TEXT]          = $row["REMARK7"];
                $put["REMARK8"][TEXT]          = $row["REMARK8"];
                $put["REMARK9"][TEXT]          = $row["REMARK9"];
                $put["REMARK10"][TEXT]         = $row["REMARK10"];
                $put["REGISTERCD"][TEXT]       = STAFFCD;
                $put["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($put, "certif_school_dat");
                $db->query($query);
            }
            Query::dbCheckIn($db);
        }
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
        }
        return;
    }
    //デリート時の内部メソッド
    function &delete()
    {
        if (!isset($this->certif_kindcd)) {
            $this->setWarning("MSG303");
            return;
        }
        knjz251Query::getDeleteQuery($this->field);
        $this->certif_kindcd = "";
        $this->setMessage("MSG201");
        return;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
