<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjd126jModel extends Model
{
    public $field = array();
    public $data  = array();
    public $cmd;
    public $term;
    public $gen_ed = "900100";        //総合学習コード
    public $select;
    public $nyuryoku;
    public $type_div;
    public $nonVisualViewCd;
    public $maxValue;

    /**
     * メンバ変数の初期化
     */
    public function knjd126jModel()
    {
        $this->field = array();
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

        $this->cmd     = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種
        $this->field["SCHOOL_KIND"] = VARS::post("SCHOOL_KIND");

        //学期コード
        $this->field["SEMESTER"] = VARS::post("SEMESTER");
        $this->field["SEMESTER2"] = VARS::post("SEMESTER2");

        //講座コード
        if ($this->field["CLASSCD"] == VARS::post("CLASSCD") && VARS::post("CHAIRCD")) {
            $this->field["CHAIRCD_SUBCLASS"] = VARS::post("CHAIRCD");
        } else {
            $this->field["CHAIRCD_SUBCLASS"] = "";
        }

        //科目コード
        $this->field["CLASSCD"] = VARS::post("CLASSCD");
        $this->field["SUBCLASSCD"] = VARS::post("SUBCLASSCD");

        if ($this->cmd == "update") {
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                $this->field["SCHOOL_KIND"] = VARS::post("H_SCHOOL_KIND");
            }
            $this->field["SEMESTER"] = VARS::post("H_SEMESTER");
            $this->field["CLASSCD"] = VARS::post("H_CLASSCD");
            $this->field["CHAIRCD_SUBCLASS"] = VARS::post("H_CHAIRCD");
        }
        //入力方法選択
        if (VARS::post("SELECT")) {
            $this->select = VARS::post("SELECT");
        }

        //入力選択
        if (VARS::post("NYURYOKU")) {
            $this->nyuryoku = VARS::post("NYURYOKU");
        }

        //入力値選択
        if (VARS::post("TYPE_DIV")) {
            $this->type_div = VARS::post("TYPE_DIV");
        }
        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向

        if ($this->field["CLASSCD"]) {
            list($classCd, $schoolKind) = explode("-", $this->field["CLASSCD"]);
            $this->schoolKindFromClassCd = $schoolKind;
            $this->maxValue = array();

            $db = Query::dbCheckOut();

            $result = $db->query(knjd126jQuery::getNameMstD085($this));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                for ($gradeCnt = $row["NAME2"]; $gradeCnt <= $row["NAME3"]; $gradeCnt++) {
                    $setGrade = sprintf("%02d", $gradeCnt);
                    $this->maxValue[$setGrade] = $row["NAMESPARE1"];
                    if ($this->Properties["useRecordDat"] == 'KIN_RECORD_DAT') {
                        if (intval($row["NAMESPARE1"]) && intval($row["NAMESPARE1"]) > 9) {
                            $this->maxValue[$setGrade] = "9";
                        }
                    }
                }
            }
            Query::dbCheckIn($db);
        }

        if ($this->cmd == "conversion" || $this->cmd == "form2_conversion") {
            //初期化
            $this->fields = array();
            for ($i=0; $i<get_count($this->data["SCHREGNO"]); $i++) {
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

    public function &getUpdateModel()
    {
        /*  更新処理 */

        //初期化
        $this->fields = array();
        for ($i=0; $i<get_count($this->data["SCHREGNO"]); $i++) {
            foreach ($this->data["STATUS"] as $code => $col) {
                $term_name = "STATUS".$code."-".$i;
                $post_term = VARS::post($term_name);

                if (isset($post_term)) {
                    $this->fields["STATUS".$code][$i] = $post_term;
                }
            }
        }

        knjd126jQuery::update($this);
        if ($this->Properties["displayHyoutei"] != "2") {
            knjd126jQuery::updateRecord($this);
        }
        $this->setMessage("MSG201");
        $this->cmd = ($this->select == "1") ? "form1" : "form2";
    }
}
