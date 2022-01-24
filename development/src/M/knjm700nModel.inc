<?php

require_once('for_php7.php');

class knjm700nModel extends Model {
    var $field  = array();      //画面データ格納
    var $setdata = array();     //表示データ配列
    var $schcntall;             //登録データ数
    var $insdata = array();     //追加データ配列
    var $upddata = array();     //更新データ配列
    var $deldata = array();     //削除データ配列
    var $cmd;                   //コマンド
    var $Year;                  //年度
    var $semester;              //学期
    var $semcheck;              //データ更新時に使用
    var $Date;                  //日付
    var $User;                  //0:管理者 1:職員
    var $control;               //コントロールマスタ

    function knjm700nModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->Year = CTRL_YEAR;

        if ($this->cmd != 'addread' && $this->cmd != 'read' && $this->cmd != 'sort') {
            $this->Date  = VARS::post("DATE");
            if ($this->Date == "") $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->field = array("COURSE"           =>  VARS::post("COURSE"),       //スクーリング種別
                                 "CHAIR"            =>  VARS::post("CHAIR"),        //講座
                                 "HR_NAME"          =>  VARS::post("HR_NAME"),        //講座
                                 "PERIOD"           =>  VARS::post("PERIOD"),       //校時
                                 "STAFF"            =>  VARS::post("STAFF"),        //担当者                                 
                                 "REMARK"           =>  VARS::post("REMARK"),       //備考
                                 "sonotaNotChk"     =>  VARS::post("sonotaNotChk"), //1ならその他をチェックしない
                                 "sonotaConfFlg"    =>  VARS::post("sonotaConfFlg") //1なら確認メッセージを表示する
                                );
        }

        if (is_array($this->schregNos)) {
            foreach ($this->schregNos as $schregNo) {
                //単位時間
                $this->field["CREDIT_TIME"][$schregNo] =VARS::post("CREDIT_TIME-".$schregNo);
            }
        }

        $this->semester = 0; //データ更新時に使用
        for ($semeCnt = 1; $semeCnt <= $this->control["学期数"]; $semeCnt++) {
            if ($this->control["学期開始日付"][$semeCnt] <= $this->Date && $this->control["学期終了日付"][$semeCnt] >= $this->Date) {
                $this->semester = $semeCnt;
            }
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

        if ($this->cmd != "add" && $this->cmd != "chdel") {
            $this->setdata = array();
        } else {
            foreach ($this->schregNos as $schregNo) {
                //単位時間
                $this->setdata["CREDIT_TIME"][$schregNo] =VARS::post("CREDIT_TIME-".$schregNo);
            }
        }

        if (VARS::get("PROGRAMID")) {
            unset($this->getSort);
            //ソート処理の初期化
            $this->sort["SRT_SCHREGNO"] = 1;
            $this->sort["SRT_HR_NAME"]  = 1;
        }

        if (VARS::get("sort")) {
            $this->getSort = VARS::get("sort");
            $this->sort[$this->getSort] = $this->sort[$this->getSort] == 1 ? 2 : 1;
        }

        $this->getPropertiesAll();  //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        $db = Query::dbCheckOut();
        $result = $db->query(knjm700nQuery::selectNameMst());
        $this->m026Array = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            list($classCd, $schoolKind, $curriculum, $subclassCd) = explode("-",$row["NAME1"]);

            $this->m026Array[$row["NAMECD2"]]["CLASSCD"] = $classCd;
            $this->m026Array[$row["NAMECD2"]]["SCHOOL_KIND"] = $schoolKind;
            $this->m026Array[$row["NAMECD2"]]["CURRICULUM_CD"] = $curriculum;
            $this->m026Array[$row["NAMECD2"]]["SUBCLASSCD"] = $subclassCd;
        }
        foreach ($this->schregNos as $schregNo) {
            $delQuery=knjm700nQuery::getDeleteQuery($this, $schregNo);
            $db->query($delQuery);
            foreach ($this->m026Array as $m026Key => $m026Val) {
                $credittime = $this->setdata["CREDIT_TIME"][$schregNo];
                //ボックスが空白でない場合 INSERT
                if ($credittime != '0') {
                    $insQuery=knjm700nQuery::getInsertQuery($this, $m026Val, $schregNo, $credittime);
                    $insResult=$db->query($insQuery);
                }
            }
        }
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

}
?>
