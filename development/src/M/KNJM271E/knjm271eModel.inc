<?php

require_once('for_php7.php');


class knjm271eModel extends Model {

    var $field  = array();      //画面データ格納
    var $setdata = array();     //表示データ配列
    var $schcntall;             //登録データ数
    var $cmd;                   //コマンド
    var $Year;                  //年度
    var $semester;              //学期
    var $Date;                  //日付
    var $User;                  //0:管理者 1:職員
    var $control;               //コントロールマスタ

    function knjm271eModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd  = VARS::request("cmd");
        $this->Year = CTRL_YEAR;

        if ($this->cmd != 'addread' && $this->cmd != 'reset') {
            if ($this->Date == "") $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->field = array("STAFF"            =>  VARS::post("STAFF"),        //添削者
                                 "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),   //科目
                                 "CHAIRCD"          =>  VARS::post("CHAIRCD"),      //講座
                                 "STANDARD_SEQ"     =>  VARS::post("STANDARD_SEQ")  //回数
                         );
        }
        $this->semester = 0;                //データ更新時に使用
        if ($this->control["学期数"] == 3) {
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
            if ($this->control["学期開始日付"]["3"] <= $this->Date && $this->control["学期終了日付"]["3"] >= $this->Date) $this->semester = 3;
        } else {
            if ($this->control["学期開始日付"]["1"] <= $this->Date && $this->control["学期終了日付"]["1"] >= $this->Date) $this->semester = 1;
            if ($this->control["学期開始日付"]["2"] <= $this->Date && $this->control["学期終了日付"]["2"] >= $this->Date) $this->semester = 2;
        }
        //年度内チェック：エラー時は、年度と学期を今年度・今学期にする。
        if ($this->semester == 0) {
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(処理年度範囲外)".str_replace("-","/",CTRL_DATE));
            return false;
        }
        if ($this->Date > str_replace("-","/",CTRL_DATE)) {
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(処理年度がオーバーしています)".str_replace("-","/",CTRL_DATE));
            return false;
        }

        if ($this->cmd != "add") {
            $this->setdata = array();
        } else {
            for ($ini = 0 ; $ini < $this->schcntall ; $ini++) {
                $this->setdata["SCHREGNO"][$ini]        = VARS::post("SCHREGNO".$ini);
                $this->setdata["REPRESENT_SEQ"][$ini]   = VARS::post("REPRESENT_SEQ".$ini);
                $this->setdata["RECEIPT_DATE"][$ini]    = VARS::post("RECEIPT_DATE".$ini);
                $this->setdata["GRAD_VALUE"][$ini]      = VARS::post("GRAD_VALUE".$ini);
                $this->setdata["PRE_GRAD_VALUE"][$ini]  = VARS::post("PRE_GRAD_VALUE".$ini);
                //◆データ変更した生徒のみ処理する。
                if ($this->setdata["PRE_GRAD_VALUE"][$ini] != $this->setdata["GRAD_VALUE"][$ini]) {
                    $this->setdata["insFlg"][$ini] = "ok";
                } else {
                    $this->setdata["insFlg"][$ini] = "skip";
                }
            }
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput() {
        $db = Query::dbCheckOut();

        if (!$this->field["STAFF"]) {
            $this->setWarning("添削者を指定して下さい。");
            return false;
        }
        if ($this->Properties["useTsushin_Repout_KouzaKonboHyoji"] == "1") {
            if (!$this->field["CHAIRCD"]) {
                $this->setWarning("講座を選択して下さい。");
                return false;
            }
        }else{
            if (!$this->field["SUBCLASSCD"]) {
                $this->setWarning("科目を選択して下さい。");
                return false;
            }
        }
        if (!$this->field["STANDARD_SEQ"]) {
            $this->setWarning("回数を選択して下さい。");
            return false;
        }

        //チェック用データ取得(STF)
        $query = knjm271eQuery::getCheckstf($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!$row) {
            $this->setWarning("添削者の講座では、ありません。");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //◆データ変更した生徒のみ処理する。
        //  初期表示した時、データを保持し、入力値と比較し、判断する。
        $this->upddata = array();
        for ($i = 0;$i < $this->schcntall;$i++) {
            if ($this->setdata["insFlg"][$i] == "skip") continue;
            //upd
            $this->upddata[] = $this->setdata["SCHREGNO"][$i].','.$this->setdata["REPRESENT_SEQ"][$i].','.$this->setdata["RECEIPT_DATE"][$i].','.$this->setdata["GRAD_VALUE"][$i];
        }
        //upd
        if (0 < get_count($this->upddata)) {
            if ($this->validateInput()){
                $this->update();
                $this->setMessage("MSG201");
            }
        } else {
            $this->setMessage("MSG202");
        }
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update() {
        knjm271eQuery::getUpdateQuery($this);
    }

}
?>
