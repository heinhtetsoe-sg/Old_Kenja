<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjz211gModel extends Model {
    
    var $field = array();
    var $data  = array();
    var $cmd;
    var $nonVisualViewCd;
    var $maxValue;
    var $viewCnt;
    var $year;
    var $semester;

    /**
     * メンバ変数の初期化
     */
    function knjz211gModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->nonVisualViewCd);
            unset($year);
            unset($schoolKind);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("DIV"              =>  VARS::post("DIV"),              //区分
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目
                             "SCHOOL_KIND"      =>  VARS::post("SCHOOL_KIND")       //校種
                            );

        if ($this->cmd == "update" || $this->cmd == "form2_update") {
            if ($this->Properties["use_prg_schoolkind"] == "1") $this->field["SCHOOL_KIND"] = VARS::post("H_SCHOOL_KIND");
            $this->field["GRADE"]           = VARS::post("H_GRADE");
            $this->field["SUBCLASSCD"]      = VARS::post("H_SUBCLASSCD");
        }

        $this->field["MOVE_ENTER"]  = VARS::post("MOVE_ENTER"); //エンター押下時の移動方向
        
        $this->maxValue = 5;
        if($this->field["SCHOOL_KIND"] && $this->field["GRADE"]){
            $db = Query::dbCheckOut();
            
            $result = $db->query(knjz211gQuery::getNameMstD085($this));
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $this->maxValue = $row["NAMESPARE1"];
            }
            Query::dbCheckIn($db);
        }
        $this->viewCnt = VARS::post("VIEWCNT");

    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }

    //更新処理
    function &getUpdateModel()
    {
        //初期化
        $this->fields = array();
        for ($i=1; $i <= $this->viewCnt; $i++) {

            foreach ($this->data["HYOUTEN"] as $code => $col) {

                $term_name = "STATUS".$i."-".$code;
                $post_term = VARS::post($term_name);

                if (isset($post_term)) {

                    $this->fields["STATUS".$i][$code] = $post_term;

                }
            }
        }
        knjz211gQuery::update($this);

        $this->setMessage("MSG201");
    }
}
?>
