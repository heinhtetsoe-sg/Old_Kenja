<?php

require_once('for_php7.php');


class knjm260Model extends Model {

    var $field  = array();  //画面データ格納
    var $setdata = array(); //表示データ配列
    var $delback = array(); //削除データ配列
    var $schnodel;          //削除データ
    var $schcntall;         //登録データ数
    var $cmd;               //コマンド
    var $Year;              //年度
    var $semester;          //学期
    var $semcheck;          //データ更新時に使用
    var $Date;              //日付
    var $User;              //0:管理者 1:職員
    var $control;           //コントロールマスタ

    function knjm260Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd         = VARS::request("cmd");
        $this->Year = CTRL_YEAR;

        //プロパティファイルの読込み
        $this->getPropertiesAll();

        if ($this->cmd != 'read' && $this->cmd != 'addread') {
            $this->Date  = VARS::post("DATE");
            if ($this->Date == "") $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->field = array("REPORTNO" =>  VARS::post("REPORTNO"),     //レポートＮｏ
                                 "CHAIR"    =>  VARS::post("CHAIR"),        //講座
                                 "REPNO"    =>  VARS::post("REPNO"),        //レポートＮｏ
                                 "SCHREGNO" =>  VARS::post("SCHREGNO")    //学籍番号
                         );
        }
        $this->semester = 0;                //データ更新時に使用
        if ($this->control["学期数"] == 3) {
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
            if ($this->control["学期開始日付"]["3"] <= $this->Date && $this->control["学期終了日付"]["3"] >= $this->Date) $this->semester = 3;                //データ更新時に使用
        } else {
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
        }
        //年度内チェック：エラー時は、年度と学期を今年度・今学期にする。
        if ($this->semester == 0) {
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(年度範囲外)");
            return false;
        }
        if ($this->Date > str_replace("-","/",CTRL_DATE)) {
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(処理日付がオーバーしています)");
            return false;
        }

        if ($this->cmd != "add" && $this->cmd != "alldel" && $this->cmd != "chdel") {
            $this->setdata = array();
        } else {
            for ($ini = 0 ; $ini < $this->schcntall ; $ini++){
                $this->setdata["DELCHK"][$ini] = VARS::post("DELCHK".$ini);
                $this->setdata["SCHREGNO2"][$ini] = VARS::post("SCHREGNO2".$ini);
                $this->setdata["NAME"][$ini] = VARS::post("NAME".$ini);
                $this->setdata["SUBCLASSNAME"][$ini] = VARS::post("SUBCLASSNAME".$ini);
                $this->setdata["REPRESENT_SEQ"][$ini] = VARS::post("REPRESENT_SEQ".$ini);
                $this->setdata["GRAD_VALUE"][$ini] = VARS::post("GRAD_VALUE".$ini);
                $this->setdata["T_TIME"][$ini] = VARS::post("T_TIME".$ini);
                $this->setdata["STANDARD_SEQ"][$ini] = VARS::post("STANDARD_SEQ".$ini);
                $this->setdata["REPORTNO"][$ini] = VARS::post("REPORTNO".$ini);
            }
        }

        if ($this->field["GRADE"]) 
            $this->field["GRADE"] = sprintf("%02d",$this->field["GRADE"]);

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput()
    {
        if (VARS::request("cmd") == 'add') {
            if (!VARS::post("DATE")) {
                $this->setWarning("評価送信日を入力して下さい。");
                return false;
            }
            if (!$this->field["REPNO"]) {
                $this->setWarning("レポートＮｏを入力して下さい。");
                return false;
            }
            //$checkRepNo = $this->Properties["useCurriculumcd"] == "1" ? "17" : "10";
            $checkRepNo = $this->Properties["useCurriculumcd"] == "1" ? "10" : "10";
            if (strlen($this->field["REPNO"]) != $checkRepNo) {
                $this->setWarning("レポートＮｏは{$checkRepNo}桁です。");
                return false;
            }
            if (substr(CTRL_YEAR,3,1) != substr($this->field["REPNO"],0,1)) {
                $this->setWarning("年度と一致しません。","レポート番号の1桁目が不正です。");
                return false;
            }
            if (!$this->field["SCHREGNO"]) {
                $this->setWarning("学籍番号を入力して下さい。");
                return false;
            }
            //チェック用データ取得(SCH)
            $db = Query::dbCheckOut();

            $query = knjm260Query::getCurriculumCd($this);
            $this->curriculumCd = $db->getOne($query);

            $query = knjm260Query::getCheckout($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row) {
                if ($this->Properties["KNJM260_NotCheck_Tyoufuku"] == "1") {
                    if ($row["GRAD_VALUE"] == "" || $row["GRAD_VALUE"] > 1) {
                        $this->setWarning("データが、重複しています");
                        return false;
                    }
                } else {
                    if ($row["GRAD_VALUE"] == "" || $row["GRAD_VALUE"] != 0) {
                        $this->setWarning("データが、重複しています");
                        return false;
                    }
                }
                if ($row["RECEIPT_DATE"] >= str_replace("/","-",$this->Date)) {    //NO002
                    $this->setWarning("日付が正しくありません");
                    return false;
                }
            }
            Query::dbCheckIn($db);

            //チェック用データ取得(レポート提出基準日)
            $db = Query::dbCheckOut();
            $query = knjm260Query::getCheckout2($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row) {
                $this->setWarning("レポート提出基準日未登録のデータです。");
                return false;
            }
            $this->field["CHAIRCD"] = $row["CHAIRCD"];
            Query::dbCheckIn($db);

            //チェック用データ取得(レポート提出基準日)
            $db = Query::dbCheckOut();
            $query = knjm260Query::getCheckout3($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row) {
                $this->setWarning("講座受講生徒データ未登録のデータです。");
                return false;
            }
            $this->field["CHAIRCD"] = $row["CHAIRCD"];
            Query::dbCheckIn($db);

        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if (VARS::request("cmd") == 'add') {
                $this->insert();
            } else if (VARS::request("cmd") == 'alldel') {
                $db = Query::dbCheckOut();
                $query = knjm260Query::deleteallcheck($this);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($row["GRAD_VALUE"] > 0) {
                    $this->setWarning("既に評価済みのデータが含まれているので削除できません");
                    return false;
                }
                Query::dbCheckIn($db);
                $this->delete();
            } else {
                $this->delback = array();
                $delc = 0;
                for ($i = 0; $i < $this->schcntall; $i++) {
                    if ($this->setdata["DELCHK"][$i]=="on") {
                        if ($this->Properties["useCurriculumcd"] == "1") {
                            $this->delback[$delc] = array("YEAR"  => $this->Year,
                                                          "SCHNO" => $this->setdata["SCHREGNO2"][$i],
                                                          "SBCLS" => substr($this->setdata["REPORTNO"][$i],8,6),
                                                          "KAISU" => substr($this->setdata["REPORTNO"][$i],14,2),
                                                          "SAISU" => substr($this->setdata["REPORTNO"][$i],16,1),
                                                          "SYDAY" => $this->setdata["RECEIPT_DATE"][$i]);
                        } else if ($this->Properties["useCurriculumcd"] == "1") {
                            $this->delback[$delc] = array("YEAR"  => $this->Year,
                                                          "SCHNO" => $this->setdata["SCHREGNO2"][$i],
                                                          "SBCLS" => substr($this->setdata["REPORTNO"][$i],1,13),
                                                          "KAISU" => substr($this->setdata["REPORTNO"][$i],14,2),
                                                          "SAISU" => substr($this->setdata["REPORTNO"][$i],16,1),
                                                          "SYDAY" => $this->setdata["RECEIPT_DATE"][$i]);
                        } else {
                            $this->delback[$delc] = array("YEAR"  => $this->Year,
                                                          "SCHNO" => $this->setdata["SCHREGNO2"][$i],
                                                          "SBCLS" => substr($this->setdata["REPORTNO"][$i],1,6),
                                                          "KAISU" => substr($this->setdata["REPORTNO"][$i],7,2),
                                                          "SAISU" => substr($this->setdata["REPORTNO"][$i],9,1),
                                                          "SYDAY" => $this->setdata["RECEIPT_DATE"][$i]);
                        }
                        $delc++;
                    }
                }
                $this->schnodel = "";
                for ($i = 0; $i < get_count($this->delback); $i++) {
                    $db = Query::dbCheckOut();
                    $query = knjm260Query::deletesiteicheck($this->delback[$i], $this);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($row){
                        Query::dbCheckIn($db);
                        $this->setWarning("既に評価済みのデータが含まれているので削除できません");
                        return false;
                    }
                    Query::dbCheckIn($db);
                }
                for ($i = 0; $i < get_count($this->delback); $i++) {
                    $this->deletesitei($this->delback[$i]);
                }
                if (get_count($this->delback) == 0) {
                    $this->setWarning("削除データを選択して下さい。");
                    return false;
                }
            }

        }
    }

    /**
     * インサート時の内部メソッド
     */
    function &insert()
    {
        knjm260Query::getDeleteIns($this);    //NO001
        knjm260Query::getInsertQuery($this);
    }
    /**
     * デリート時の内部メソッド
     */
    function &delete()
    {
        knjm260Query::getDeleteQuery($this);
    }
    function &deletesitei($data)
    {
        knjm260Query::getDeleteQuerysitei($data, $this);
    }

}
?>
