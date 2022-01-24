<?php
class knje372cModel extends Model {
    var $cmd;
    var $year;
    var $semester;
    var $field;
    var $schregnoList = array();
    var $schregnoRowList = array();

    //メンバ変数の初期化
    function knje372cModel()
    {
        //学籍処理日取得
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->semester);
            unset($this->field);
        }

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;

        $this->field = array(
            "GRADE_HR_CLASS"     =>     VARS::post("GRADE_HR_CLASS"),
        );

        //活動内容サイズ
        $this->moji = "25";
        $this->gyou = "4";

        $this->listData = array();

    }

    //入力項目チェック
    function validateInput()
    {
        foreach ($this->schregnoList as $schregno) {
            $activityContent = $this->schField[$schregno]["ACTIVITY_CONTENT"];
            //文字数チェック
            if ($this->validate_row_cnt($activityContent, $this->moji * 2) > $this->gyou) {
                $this->setWarning("MSG915", "(校友会活動内容) は全角{$this->moji}文字×{$this->gyou}行までです。");
                return false;
            }

            //学科番号チェック
            $chkStartFlg = false;
            $dupChkArray = array();
            for ($i = 1; $i <= $this->limitCnt; $i++) {
                $hopeOrder = sprintf("%02d", $i);
                $departmentCd = $this->schField[$schregno]["HOPE_ORDER"][$hopeOrder];

                //1.学科番号が志望順に登録されているかチェック(順位を飛ばして登録されたらエラー)
                if (!$chkStartFlg && $departmentCd == "") {
                    $chkStartFlg = true;
                }
                if ($chkStartFlg && $departmentCd != "") {
                    $this->setWarning("MSG901", "志望順に登録してください。");
                    return false;
                }
    
                //2.異なる順位に同じ学科番号が登録されているかチェック
                if ($departmentCd != "") {
                    if (in_array($departmentCd, $dupChkArray)) {
                        $this->setWarning("MSG901", "学科番号が重複しています。");
                        return false;
                    }
                    $dupChkArray[] = $departmentCd;
                }
            }

        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel()
    {
        $this->schField = array();

        foreach ($this->schregnoList as $schregno) {
            $schRownumberArray = $this->schregnoRowList[$schregno];
            $number = 1;
            foreach ($schRownumberArray as $index => $rownumber) {
                for ($colnumber = 1; $colnumber <= 21; $colnumber++) {
                    $postterm = "HOPEORDER{$colnumber}-{$rownumber}";
                    $this->schField[$schregno]["HOPE_ORDER"][sprintf("%02d", $number)] = VARS::post($postterm);
                    $number++;
                }
            }

            $this->schField[$schregno]["ACTIVITY_CD"]       = VARS::post("ACTIVITY_CD_".$schregno);
            $this->schField[$schregno]["ACTIVITY_CONTENT"]  = VARS::post("ACTIVITY_CONTENT_".$schregno);
            $this->schField[$schregno]["DECLINE_FLG"]       = VARS::post("DECLINE_FLG_".$schregno);
        }

        //入力チェック
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
    }

    //アップデート時の内部メソッド
    function &update()
    {
        $result = knje372cQuery::getUpdateQuery($this);
        return $result;
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
