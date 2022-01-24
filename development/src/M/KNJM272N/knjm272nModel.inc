<?php

require_once('for_php7.php');

class knjm272nModel extends Model {

    var $field  = array();      //画面データ格納
    var $setdata = array();     //表示データ配列
    var $schcntall;             //登録データ数
    var $cmd;                   //コマンド
    var $Year;                  //年度
    var $semester;              //学期
    var $Date;                  //日付
    var $User;                  //0:管理者 1:職員
    var $control;               //コントロールマスタ
    var $schregno;

    function knjm272nModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            unset($this->schregno);
        }
        if (VARS::get("SCHREGNO")) {
            $this->schregno = VARS::get("SCHREGNO");
        }

        $this->cmd  = VARS::request("cmd");
        $this->Year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;                //データ更新時に使用

        if ($this->cmd != 'addread' && $this->cmd != 'reset') {
            if ($this->Date == "") $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->field = array("STAFF"            =>  VARS::post("STAFF")         //添削者
                         );
        }

        $this->updData = array();
        if ($this->cmd == "add") {
            foreach ($this->subArray as $subclassCd => $subVal) {

                $Row = array();
                //科目名
                $Row["SUBCLASSNAME"] = $subVal["SUBCLASSNAME"];
                for ($seqCnt = 1; $seqCnt <= 12; $seqCnt++) {

                    $seqVal = $subVal["SEQ_{$seqCnt}"];
                    $soeji = $subclassCd."_".$seqVal["SEQ"];
                    if (strlen($seqVal["SEQ"]) > 0) {
                        //表示した状態と不一致の場合のみ更新対象
                        $postGradVal = VARS::post("GRAD_VALUE_{$soeji}");
                        $postGradDate = VARS::post("GRAD_INPUT_DATE_{$soeji}");
                        list($month, $day) = explode("/", $postGradDate);
                        if ($month && $day) {
                            $setDate = $month < 4 ? (CTRL_YEAR + 1)."-".sprintf("%02d", $month)."-".sprintf("%02d", $day) : CTRL_YEAR."-".sprintf("%02d", $month)."-".sprintf("%02d", $day);
                        } else {
                            $setDate = "";
                        }
                        $postGradDate = sprintf("%02d", $month)."/".sprintf("%02d", $day);
                        $checkGradValu = strlen($subVal["GRAD_VALUE{$seqVal["SEQ"]}"]) > 0 ? $subVal["GRAD_VALUE{$seqVal["SEQ"]}"] : "9999";
                        $checkGradDate = $subVal["GRAD_INPUT_DATE{$seqVal["SEQ"]}"] ? $subVal["GRAD_INPUT_DATE{$seqVal["SEQ"]}"] : "00/00";
                        if (($checkGradValu != $postGradVal) ||
                            ($checkGradDate != $postGradDate)
                            ) {
                            $this->updData[] = $subclassCd.','.$seqVal["SEQ"].',0,'.$setDate.','.$postGradVal;
                        }
                    }

                }
            }
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        if (0 < get_count($this->updData)) {
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
        knjm272nQuery::getUpdateQuery($this);
    }

}
?>
