<?php

require_once('for_php7.php');


class knjd124cModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $term;
    public $usePerfect;
    public $lastColumn;//CSVの最後の列の値

    public function knjd124cModel()
    {
    }

    public function init()
    {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->cmd     = VARS::request("cmd");

        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        //学期コード
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        //科目コード
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        //講座コード
        if ($this->cmd!="subclasscd"&&VARS::post("CHAIRCD")) {
            $this->field["CHAIRCD"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD"] = "";
        }

        if ($this->cmd == "update") {
            $this->field["SEMESTER"] = VARS::post("H_SEMESTER");
            $this->field["SUBCLASSCD"] = VARS::post("H_SUBCLASSCD");
            $this->field["CHAIRCD"] = VARS::post("H_CHAIRCD");
        }

        for ($i=1; $i<=4; $i++) {
            $this->field["CHK_COMP_VALUE".$i] = VARS::post("CHK_COMP_VALUE".$i);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->usePerfect = (strlen($this->Properties["usePerfect"])) ? $this->Properties["usePerfect"] : false;

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        /*  更新処理 */

        //学期成績データ項目名を配列にする。
        $term = array("SEM1_SCORE","SEM2_SCORE","SEM3_SCORE","PROV_FLG","GRAD_SCORE");

        //初期化
        $this->fields = array();
        $this->isScoreAllNull = array();
        for ($j=0; $j < get_count($term); $j++) {
            $this->isScoreAllNull[$term[$j]] = false;
            for ($i=0; $i<get_count($this->data["SCHREGNO"]); $i++) {
                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);

                if (isset($post_term)) {
                    //未入力・見込点情報が入ってたら成績は空
                    $pos1 = strpos($post_term, "(");
                    $pos2 = strpos($post_term, ")");
                    $pos3 = strpos($post_term, "[");
                    $pos4 = strpos($post_term, "]");
                    if ($post_term != "*" && $pos1 === false && $pos2 === false && $pos3 === false && $pos4 === false) {
                        $this->fields[$term[$j]."_DI"][] = "";
                        $this->fields[$term[$j]][]       = $post_term;
                    } else {
                        $retVal = str_replace("(", "", $post_term);
                        $retVal = str_replace(")", "", $retVal);
                        $retVal = str_replace("[", "", $retVal);
                        $retVal = str_replace("]", "", $retVal);
                        $this->fields[$term[$j]."_DI"][] = $retVal;
                        $this->fields[$term[$j]][]       = "";
                    }

                    if (strlen($post_term)) {
                        $this->isScoreAllNull[$term[$j]] = true;
                    }
                } else {
                    $this->fields[$term[$j]][]       = $this->data[$term_name];
                    $this->fields[$term[$j]."_DI"][] = $this->di_data[$term_name];

                    if (strlen($this->data[$term_name])) {
                        $this->isScoreAllNull[$term[$j]] = true;
                    }
                }
            }
        }

        knjd124cQuery::update($this);
        knjd124cQuery::updateExecuted2($this);
        $this->setMessage("MSG201");
    }
}
