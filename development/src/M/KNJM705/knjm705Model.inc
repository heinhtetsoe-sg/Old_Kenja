<?php

require_once('for_php7.php');

class knjm705Model extends Model {
    var $field  = array();      //画面データ格納
    var $setdata = array();     //表示データ配列
    var $schcntall;             //登録データ数
    var $insdata = array();     //追加データ配列
    var $deldata = array();     //削除データ配列
    var $cmd;                   //コマンド
    var $Year;                  //年度
    var $control;               //コントロールマスタ

    function knjm705Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->cmd = VARS::request("cmd");

        if ($this->cmd != 'read' && $this->cmd != 'sort') {
            $this->field = array("YEAR"         =>  CTRL_YEAR,                  //年度
                                 "SEMESTER"     =>  CTRL_SEMESTER,              //学期
                                 "CLASSCD"           =>  ($this->cmd == 'add') ? VARS::post("DIS_CLASSCD")     : VARS::post("CLASSCD"),       //教科
                                 "SUBCLASSCD"        =>  ($this->cmd == 'add') ? VARS::post("DIS_SUBCLASSCD")  : VARS::post("SUBCLASSCD"),    //科目
                                 "CHAIRCD"           =>  ($this->cmd == 'add') ? VARS::post("DIS_CHAIRCD")     : VARS::post("CHAIRCD"),       //講座
                                 "ATTENDDATE"        =>  ($this->cmd == 'add') ? VARS::post("DIS_ATTENDDATE")  : ($this->cmd == "" ? str_replace("-", "/", CTRL_DATE) : VARS::post("ATTENDDATE")),   //出席日付
                                 "PERIODF"           =>  ($this->cmd == 'add') ? VARS::post("DIS_PERIODF")     : VARS::post("PERIODF"),       //校時
                                 "CREDIT_TIME"       =>  ($this->cmd == 'add') ? VARS::post("DIS_CREDIT_TIME") : VARS::post("CREDIT_TIME"),   //単位時間
                                );
        }

        if ($this->cmd != "add" && $this->cmd != "chdel") {
            $this->setdata = array();
        } else {
            foreach ($this->schregNos as $schregNo) {
                $this->setdata["SCHREGNO"][$schregNo]   = VARS::post("SCHREGNO".$schregNo);
                $this->setdata["CHECK"][$schregNo]      = VARS::post("CHECK-".$schregNo);
                //備考
                $this->setdata["REMARK"][$schregNo]     = VARS::post("REMARK".$schregNo);
                $this->setdata["PRE_REMARK"][$schregNo] = VARS::post("PRE_REMARK".$schregNo);
                //チェックした生徒のみ処理する。
                if ($this->setdata["CHECK"][$schregNo] == '1') {
                    $this->setdata["insFlg"][$schregNo] = "ok";
                } else {
                    $this->setdata["insFlg"][$schregNo] = "skip";
                }
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
    }

    //入力項目チェック
    function validateInput() {

        if (VARS::request("cmd") == 'add') {

            $hissuCheck = array("CLASSCD"      =>  "教科",
                                "SUBCLASSCD"   =>  "科目",
                                "CHAIRCD"      =>  "講座",
                                "ATTENDDATE"   =>  "出席日付",
                                "PERIODF"      =>  "校時",
                                "CREDIT_TIME"  =>  "単位時間",
            );

            foreach ($hissuCheck as $hissuKey => $hissuVal) {
                if (strlen($this->field[$hissuKey]) == 0) {
                    $this->setWarning($hissuVal."を入力して下さい。");
                    return false;
                }
            }

            //範囲チェック
            if ($this->control["学期開始日付"]["9"] > $this->field["ATTENDDATE"] || $this->control["学期終了日付"]["9"] < $this->field["ATTENDDATE"]) {
                $this->setWarning("MSG915","(年度範囲外)");
                return false;
            }
            if ($this->field["ATTENDDATE"] > str_replace("-", "/", CTRL_DATE)) {
                $this->setWarning("MSG915","(処理日付がオーバーしています)");
                return false;
            }

            $db = Query::dbCheckOut();
            //ins
            for ($i = 0; $i < get_count($this->insdata); $i++) {
                list($schregno, $remark) = preg_split("/,/", $this->insdata[$i]);
                //名簿チェック
                $query = knjm705Query::getChairStdCnt($this, $schregno);
                $chairStd = $db->getOne($query);
                if ($chairStd == 0) {
                    $this->setWarning("指定講座の名簿に生徒が存在しません。");
                    return false;
                }
            }
            Query::dbCheckIn($db);
        }
        return true;
    }

    // 削除用のモデルを返します
    function &getDeleteModel() {
        $this->deldata = array();
        foreach ($this->schregNos as $schregNo) {
            if ($this->setdata["insFlg"][$schregNo] == "skip") continue;
            $this->deldata[] = $this->setdata["SCHREGNO"][$schregNo];
        }
        knjm705Query::getDeleteQuerysitei($this->field, $this);
        $this->setMessage("MSG201");
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //◆データ変更した生徒のみ処理する。
        //  初期表示した時、データを保持し、入力値と比較し、判断する。
        $this->insdata = array();
        $this->deldata = array();
        foreach ($this->schregNos as $schregNo) {
            if ($this->setdata["insFlg"][$schregNo] == "skip") continue;
            //文字数チェック
            if (strlen($this->setdata["REMARK"][$schregNo]) > 90) {
                $this->setWarning("MSG915", strlen($this->setdata["REMARK"][$schregNo])."(備考は90バイトまでです)");
                return false;
            }
            $this->deldata[] = $this->setdata["SCHREGNO"][$schregNo];
            $this->insdata[] = $this->setdata["SCHREGNO"][$schregNo].','.$this->setdata["REMARK"][$schregNo];
        }
        //ins
        if (0 < get_count($this->insdata)) {
            if ($this->validateInput()){
                $this->insert();
            }
        }
        if (0 < get_count($this->insdata)) {
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG202");
        }
    }

    //インサート時の内部メソッド
    function &insert() {
        $this->deletesitei($this->field);
        knjm705Query::getInsertQuery($this);
    }
    //削除
    function &deletesitei($data) {
        knjm705Query::getDeleteQuerysitei($data, $this);
    }
}
?>
