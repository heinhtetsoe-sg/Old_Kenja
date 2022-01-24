<?php

require_once('for_php7.php');


class knjm271nModel extends Model {

    var $field  = array();      //画面データ格納
    var $stdDbData = array();   //表示データDB配列
    var $schcntall;             //登録データ数
    var $cmd;                   //コマンド
    var $semester;              //学期
    var $Date;                  //日付
    var $User;                  //0:管理者 1:職員
    var $control;               //コントロールマスタ
    var $maxreceipt_date;       //受付日付の最大値

    function knjm271nModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd  = VARS::request("cmd");
        $this->maxSeq = 12;

        if ($this->cmd != 'sort') {
            $this->Date  = VARS::post("DATE");
            $this->field = array("SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),   //科目
                                 "STANDARD_SEQ"     =>  VARS::post("STANDARD_SEQ"), //回数
                                 "STAFF"            =>  VARS::post("STAFF")         //添削者
                         );
        }

        if (VARS::get("PROGRAMID")) {
            $this->Date = str_replace("-", "/", CTRL_DATE);
            unset($this->getSort);
            //ソート処理の初期化
            $this->sort["SRT_CLASS"] = 1;
            $this->sort["SRT_SCHREGNO"] = 1;
            $this->sort["SRT_NAME"] = 1;
        }

        $this->semester = 0; //データ更新時に使用
        for ($semeCnt = 1; $semeCnt <= $this->control["学期数"]; $semeCnt++) {
            if ($this->control["学期開始日付"][$semeCnt] <= $this->Date && $this->control["学期終了日付"][$semeCnt] >= $this->Date) {
                $this->semester = $semeCnt;
            }
        }
        //年度内チェック：エラー時は、年度と学期を今年度・今学期にする。
        if ($this->semester == 0){
            $this->setWarning("MSG915", "(".CTRL_YEAR."年度処理範囲外)\\n" . str_replace("-", "/", $this->Date));
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            return false;
        }

        //更新対象配列
        $this->updData = array();
        if ($this->cmd == "add") {
            foreach ($this->stdDbData as $schregNo => $schVal) {
                //表示した状態と不一致の場合のみ更新対象
                if ($schVal["GRAD_VALUE{$this->field["STANDARD_SEQ"]}"] != VARS::post("GRAD_VALUE_{$schregNo}")) {
                    $this->updData[$schregNo]["GRAD_VALUE"] = VARS::post("GRAD_VALUE_{$schregNo}");
                }
            }
        }

        if (VARS::get("sort")) {
            $this->getSort = VARS::get("sort");
            $this->sort[$this->getSort] = $this->sort[$this->getSort] == 1 ? 2 : 1;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knjm271nQuery::getInsertQuery($this);
        }
        $this->setMessage("MSG201");
    }

}
?>
