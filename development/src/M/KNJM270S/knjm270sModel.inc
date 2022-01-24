<?php

require_once('for_php7.php');


class knjm270sModel extends Model {

    var $field  = array();      //画面データ格納
    var $setdata = array();     //表示データ配列
    var $delback = array();     //削除データ配列
    var $schnodel;              //削除データ
    var $schcntall;             //登録データ数
    var $cmd;                   //コマンド
    var $sch;                   //更新用
    var $schnm;                 //更新用
    var $sucl;                  //更新用
    var $date;                  //更新用
    var $stsq;                  //更新用
    var $rsq ;                  //更新用
    var $gval;                  //更新用
    var $chir;                  //更新用
    var $stf ;                  //更新用
    var $rdat;                  //更新用
    var $schregno;              //サブフォーム用
    var $remark;                //サブフォーム用
    var $Year;                  //年度
    var $semester;              //学期
    var $semcheck;              //データ更新時に使用
    var $Date;                  //日付
    var $User;                  //0:管理者 1:職員
    var $control;               //コントロールマスタ
    var $maxreceipt_date;       //受付日付の最大値 NO002

    function knjm270sModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd         = VARS::request("cmd");
        $this->Year = CTRL_YEAR;

        if ($this->cmd != 'update' && $this->cmd != 'read' && $this->cmd != 'addread' && $this->cmd != 'reset'){
            $this->Date  = VARS::post("DATE");
            if ($this->Date == "") $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->field = array("INPUT_RADIO"      =>  VARS::post("INPUT_RADIO"),  //入力方法
                                 "REPORTNO"         =>  VARS::post("REPORTNO"),     //レポートＮｏ
                                 "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),   //科目
                                 "STANDARD_SEQ"     =>  VARS::post("STANDARD_SEQ"), //回数
                                 "CHAIR"            =>  VARS::post("CHAIR"),        //講座
                                 "STAFF"            =>  VARS::post("STAFF"),        //添削者
                                 "REPNO"            =>  VARS::post("REPNO"),        //レポートＮｏ
                                 "SCHREGNO"         =>  VARS::post("SCHREGNO"),     //学籍番号
                                 "HYOUKA"           =>  VARS::post("HYOUKA")        //評価
                         );
        }
        $this->semester = 0;                //データ更新時に使用
        if ($this->control["学期数"] == 3){
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
            if ($this->control["学期開始日付"]["3"] <= $this->Date && $this->control["学期終了日付"]["3"] >= $this->Date) $this->semester = 3;              //データ更新時に使用
        }else {
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
        }
        //年度内チェック：エラー時は、年度と学期を今年度・今学期にする。
        if ($this->semester == 0){
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(処理年度範囲外)".str_replace("-","/",CTRL_DATE));
            return false;
        }
        if ($this->Date > str_replace("-","/",CTRL_DATE)){
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(処理年度がオーバーしています)".str_replace("-","/",CTRL_DATE));
            return false;
        }

        if ($this->cmd != "update" && $this->cmd != "add" && $this->cmd != "chdel"){
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
                $this->setdata["STANDARD_SEQ"][$ini] = VARS::post("STANDARD_SEQ".$ini);
            }
        }

        if ($this->field["GRADE"]) 
            $this->field["GRADE"] = sprintf("%02d",$this->field["GRADE"]);

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput()
    {
        $db = Query::dbCheckOut();

        if (VARS::request("cmd") == 'add'){
            if (!VARS::post("DATE")) {
                $this->setWarning("評価送信日を入力して下さい。");
                return false;
            }
            if (!$this->field["STAFF"]) {
                $this->setWarning("添削者を指定して下さい。");
                return false;
            }
            if ($this->field["INPUT_RADIO"] == "1") {
                if (!$this->field["REPNO"]) {
                    $this->setWarning("レポートＮｏを入力して下さい。");
                    return false;
                }
                $checkRepNo = "10";
                if (strlen($this->field["REPNO"]) != $checkRepNo) {
                    $this->setWarning("レポートＮｏは{$checkRepNo}桁です。");
                    return false;
                }
                if (substr(CTRL_YEAR,3,1) != substr($this->field["REPNO"], 0, 1)) {
                    $this->setWarning("年度と一致しません。","レポート番号の9桁目が不正です。");
                    return false;
                }
            } else {
                if (!$this->field["SUBCLASSCD"]) {
                    $this->setWarning("科目を選択して下さい。");
                    return false;
                }
                if (!$this->field["STANDARD_SEQ"]) {
                    $this->setWarning("回数を選択して下さい。");
                    return false;
                }
                if (!$this->field["SCHREGNO"]) {
                    $this->setWarning("学籍番号を入力して下さい。");
                    return false;
                }
            }
            if ($this->field["HYOUKA"] == '') {
                $this->setWarning("評価を入力して下さい。");
                return false;
            }
            //チェック用データ取得(SCH)
            $query = knjm270sQuery::getCheckout($this);
            $cnt = $db->getOne($query);
            if ($cnt == 0) {
                $this->setWarning("受付登録がされていません");
                return false;
            } else {
                $query = knjm270sQuery::getMaxReceiptDate($this);
                $this->maxreceipt_date = $db->getOne($query);
            }

            //チェック用データ取得(STF)
            $query = knjm270sQuery::getCheckstf($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if (!$row){
                $this->setWarning("添削者の講座では、ありません。");
                return false;
            }

            //チェック用データ取得(評価)
            $query = knjm270sQuery::getCheckhyouka($this->field["HYOUKA"]);
            $row   = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row){
                $this->setWarning("評価データに登録されていない番号です");
                return false;
            }

            //チェック用データ取得(評価登録済)
            $query = knjm270sQuery::getCheckGrad($this);
            $cnt   = $db->getOne($query);
            if ($cnt > 0){
                $this->setWarning("既に評価登録されています");
                return false;
            }
        }
        Query::dbCheckIn($db);

        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if (VARS::request("cmd") == 'add') {
                $this->insert();
            } else if(VARS::request("cmd") == 'update') {
                $this->update();
            } else {
                $this->delback = array();
                $delc = 0;
                for ($i = 0;$i < $this->schcntall;$i++){
                    if ($this->setdata["DELCHK"][$i]=="on"){
                        $curriculumCd = substr($this->setdata["REPORTNO"][$i], 1, 1);
                        $subclassCd = substr($this->setdata["REPORTNO"][$i], 2, 6);
                        $setSubclass = substr($subclassCd, 0, 2)."H".$curriculumCd.$subclassCd;
                        $this->delback[$delc] = array("YEAR"  => $this->Year,
                                                      "SCHNO" => $this->setdata["SCHREGNO2"][$i],
                                                      "SBCLS" => $setSubclass,
                                                      "KAISU" => $this->setdata["STANDARD_SEQ"][$i],
                                                      "SAISU" => "0",
                                                      "SYDAY" => $this->setdata["RECEIPT_DATE"][$i]);
                        $delc++;
                    }
                }
                $this->schnodel = "";
                for ($i = 0; $i < get_count($this->delback); $i++) {
                    $db = Query::dbCheckOut();
                    //再提出回数チェック
                    $query = knjm270sQuery::getMaxRepresentSeq($this->delback[$i]);
                    $maxRepSeq = $db->getOne($query);
                    if ($maxRepSeq != $this->delback[$i]["SAISU"]){
                        $this->setWarning("再提出の受付が行われているため削除できません");
                        return false;
                    }
                    Query::dbCheckIn($db);
                }
                for ($i = 0;$i < get_count($this->delback);$i++) {
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
        knjm270sQuery::getInsertQuery($this);
    }
    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        knjm270sQuery::getUpdateQuery($this);
    }
    /**
     * デリート時の内部メソッド
     */
    function &delete()
    {
        knjm270sQuery::getDeleteQuery($this);
    }
    function &deletesitei($data)
    {
        knjm270sQuery::getDeleteQuerysitei($data, $this);
    }

}
?>
