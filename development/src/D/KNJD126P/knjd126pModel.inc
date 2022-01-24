<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd126pModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $term;
    public $select;
    public $nyuryoku;
    public $type_div;
    public $nonVisualViewCd;
    public $maxValue;

    /**
     * メンバ変数の初期化
     */
    public function knjd126pModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->select);
            unset($this->nyuryoku);
            unset($this->type_div);
            unset($this->nonVisualViewCd);
        }

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期（観点データ用）
                             "SEMESTER2"        =>  VARS::post("SEMESTER2"),        //学期
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND")       //校種
                            );

        if ($this->cmd == "update" || $this->cmd == "form2_update") {
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                $this->field["SCHOOL_KIND"] = VARS::post("H_SCHOOL_KIND");
            }
            $this->field["SEMESTER"]        = VARS::post("H_SEMESTER");
            $this->field["GRADE_HR_CLASS"]  = VARS::post("H_GRADE_HR_CLASS");
            $this->field["SUBCLASSCD"]      = VARS::post("H_SUBCLASSCD");
        }

        if (VARS::post("SELECT")) {
            $this->select = VARS::post("SELECT");
        }
        if (VARS::post("NYURYOKU")) {
            $this->nyuryoku = VARS::post("NYURYOKU");
        }
        if (VARS::post("TYPE_DIV")) {
            $this->type_div = VARS::post("TYPE_DIV");
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向

        $this->maxValue = 5;
        if ($this->field["SCHOOL_KIND"] && $this->field["GRADE_HR_CLASS"]) {
            $this->field["GRADE"] = substr($this->field["GRADE_HR_CLASS"], 0, 2);
            $db = Query::dbCheckOut();

            $result = $db->query(knjd126pQuery::getNameMstD085($this));

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->maxValue = $row["NAMESPARE1"];
                if ($this->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
                    if (intval($row["NAMESPARE1"]) && intval($row["NAMESPARE1"]) > 9) {
                        $this->maxValue = "9";
                    }
                }
            }
            Query::dbCheckIn($db);
        }

        if ($this->cmd == "conversion" || $this->cmd == "form2_conversion") {
            //初期化
            $this->fields = array();
            for ($i=0; $i < get_count($this->data["SCHREGNO"]); $i++) {
                foreach ($this->data["STATUS"] as $code => $col) {
                    $term_name = "STATUS".$code."-".$i;
                    $post_term = VARS::post($term_name);

                    if (isset($post_term)) {
                        $this->fields["STATUS".$code][$i] = $post_term;
                    }
                }
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        for ($i=0; $i < get_count($this->data["SCHREGNO"]); $i++) {
            foreach ($this->data["STATUS"] as $code => $col) {
                $term_name = "STATUS".$code."-".$i;
                $post_term = VARS::post($term_name);

                if (isset($post_term)) {
                    $this->fields["STATUS".$code][$i] = $post_term;
                }
            }
        }

        knjd126pQuery::update($this);
        if ($this->Properties["displayHyoutei"] != "2") {
            knjd126pQuery::updateRecord($this);
        }
        $this->setMessage("MSG201");
    }
}
