<?php

require_once('for_php7.php');

class knjf016Model extends Model {
    var $fields = array();
    var $cmd;
    var $year;
    var $semester;
    var $hr_class;
    var $no;
    var $data_cnt;
    var $control;

    function knjf016Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::request("PROGRAMID") != "") {
            $this->year = CTRL_YEAR;
            $this->semester = CTRL_SEMESTER;
        }

        $this->data_cnt = VARS::post("DATA_CNT");

        $this->cmd = VARS::request("cmd");

        //更新中、サブミットする項目使用不可
        //よって、サブミットする項目の値は、hiddenから取得
        if ($this->cmd == 'update') {
            $this->hr_class     = VARS::post("H_HR_CLASS");
            $this->no           = VARS::post("H_NO");
        } else {
            $this->hr_class     = VARS::post("HR_CLASS");
            $this->no           = VARS::post("NO");
        }

        //初期化
        $this->fields = array();
        $this->inputFlg = array();
        if ($this->cmd == 'update') {
            $paramArray = $this->getParam();
            for ($g = 0; $g < $this->data_cnt; $g++) {
                $this->inputFlg[$g] = "NG";
                foreach ($paramArray as $val) {
                    $this->fields[$val][] = VARS::post($val."-".$g);
                    if (!in_array($val,array("ATTENDNO","NAME_SHOW","REGD_SCHREGNO","SCHOOL_KIND","HIDDENDATE"))) {
                        if (strlen($this->fields[$val][$g])) {
                            $this->inputFlg[$g] = "OK";
                        }
                    }
                }
            }
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function getParam() {
        $paramArray = array(
            'ATTENDNO',
            'NAME_SHOW',
            'REGD_SCHREGNO',
            'SCHOOL_KIND',
            'HIDDENDATE',
            'DATE',
            'HEIGHT',
            'WEIGHT',
            'SITHEIGHT',
            'R_BAREVISION_MARK',
            'L_BAREVISION_MARK',
            'R_VISION_MARK',
            'L_VISION_MARK',
            );
        return $paramArray;
    }

    //入力項目チェック
    function validateInput() {
        for ($g = 0; $g < $this->data_cnt; $g++) {
            //エラーメッセージに出席番号・氏名を表示（誰がエラーかわかるように）
            $atno_name = $this->fields["ATTENDNO"][$g].'番 '.$this->fields["NAME_SHOW"][$g];
            //キーの更新チェック
            if (!strlen($this->fields["REGD_SCHREGNO"][$g])) {
                $this->setWarning("MSG301", $atno_name."：(学籍番号)");
                return false;
            }
            //データを入力されている時は、健康診断実施日のnullチェックをする。
            if ($this->inputFlg[$g] == "OK" && !strlen($this->fields["DATE"][$g])) {
                $this->setWarning("MSG301", $atno_name."：(健康診断実施日)");
                return false;
            }
            //値チェック
            $height = explode(".",$this->fields["HEIGHT"][$g]);
            $weight = explode(".",$this->fields["WEIGHT"][$g]);
            $sitheight = explode(".",$this->fields["SITHEIGHT"][$g]);
            if (strlen($height[0]) > 3) {
                $this->setWarning("MSG901",$atno_name."：(身長)");
                return false;
            } else if (strlen($height[1]) > 1) {
                $this->setWarning("MSG901",$atno_name."：(身長) 小数点第一位まで");
                return false;
            }
            if (strlen($weight[0]) > 3) {
                $this->setWarning("MSG901",$atno_name."：(体重)");
                return false;
            } else if (strlen($weight[1]) > 1) {
                $this->setWarning("MSG901",$atno_name."：(体重) 小数点第一位まで");
                return false;
            }
            if (strlen($sitheight[0]) > 3) {
                $this->setWarning("MSG901",$atno_name."：(座高)");
                return false;
            } else if (strlen($sitheight[1]) > 1) {
                $this->setWarning("MSG901",$atno_name."：(座高) 小数点第一位まで");
                return false;
            }
        }
        return true;
    }

    //追加および更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()){
            knjf016Query::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

}
?>
