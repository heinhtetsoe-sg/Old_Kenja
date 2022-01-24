<?php

require_once('for_php7.php');


class knje063p_2Model extends Model {

    var $field        = array();
    var $cmd;
    var $schregno;
    var $class_select;
    var $subclasscd;
    var $sort;
    var $year = array();
    var $knje030cd;        //primary
    var $knje030year;
    var $knje030schreg;

    function knje063p_2Model() {
        $this->sort["YEAR"] = 1;
        $this->year[0] = CTRL_YEAR;
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            $this->year = array();
        }
        $this->grade_range = 3;
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "GRADE_NAME1"      =>  VARS::post("GRADE_NAME1"),      //学年(年次)
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //科目コード
                             "CLASSNAME"        =>  VARS::post("CLASSNAME"),        //教科名
                             "CLASSABBV"        =>  VARS::post("CLASSABBV"),        //教科略略称
                             "CLASSNAME_ENG"    =>  VARS::post("CLASSNAME_ENG"),    //教科英
                             "CLASSABBV_ENG"    =>  VARS::post("CLASSABBV_ENG"),    //教科英略称
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目コード
                             "SUBCLASSNAME"     =>  VARS::post("SUBCLASSNAME"),     //科目名
                             "SUBCLASSABBV"     =>  VARS::post("SUBCLASSABBV"),     //科目略称
                             "SUBCLASSNAME_ENG" =>  VARS::post("SUBCLASSNAME_ENG"), //科目英
                             "SUBCLASSABBV_ENG" =>  VARS::post("SUBCLASSABBV_ENG"), //科目英略称
                             "REMARK"           =>  VARS::post("REMARK")            //備考
                             );                
        $this->subclasscd = $this->field["CLASSCD"].$this->field["SUBCLASSCD"];

        for ($i = 0; $i < $this->grade_range; $i++) {    
            $this->field[$i]["VALUATION"]   = VARS::post("VALUATION".$i);
            $this->field[$i]["GET_CREDIT"]  = VARS::post("GET_CREDIT".$i);
            $this->field[$i]["ADD_CREDIT"]  = VARS::post("ADD_CREDIT".$i);
            $this->field[$i]["COMP_CREDIT"] = VARS::post("COMP_CREDIT".$i);
        }

        $this->checked  = VARS::request("CHECKED");          //チェック
        $this->checked2 = VARS::request("CHECKED2");          //チェック
        $this->cmd          = VARS::request("cmd");
        if ($this->cmd == "edit" && is_string($this->checked)) {
            $a = explode(",", $this->checked);
            $this->editYear    = $a[0];
            $this->annual      = $a[1];
            $this->subclasscd  = $a[2];
        } else if ($this->cmd == "right" ) {
            $this->editYear    = NULL;
            $this->annual      = NULL;
            $this->subclasscd  = NULL;
        }

        $this->knje030cd      = VARS::request("SUBCLASSCD");
        $this->knje030schreg  = VARS::request("SCHREGNO");
        $this->knje030year    = VARS::request("YEAR");

        //教科コンボより
        if (VARS::request("CLASSCD") =="") {
            $classcd = substr($this->subclasscd,0,2);
        } else {
            $classcd = VARS::request("CLASSCD");
        }

        $this->class_select  = $classcd;

        if (VARS::request("SCHREGNO")) {
            $this->schregno = VARS::request("SCHREGNO");
        }
        if (VARS::get("NAME")) {
            $this->name = VARS::get("NAME");
            $this->year = array(); //左のリストから選択されたら初期化
        }
        if (VARS::get("GRADE")) {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS")) {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if (VARS::get("ATTENDNO")) {
            $this->attendno = VARS::get("ATTENDNO");
        }
        if (VARS::get("init") == 1) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            $this->year = array();
        }
        if (VARS::post("YEAR")!="") {
            $this->year[] = VARS::post("YEAR");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }
    /**
     * 全体表示用モデルを返します
    */
    function &getMainModel() {
        //ソート用 番号、席次
        $sort = array("YEAR","SUBCLASSCD");
        //ソートが押下
        if (trim(VARS::request("sort")) != "") {
            foreach ($sort as $field) {
                if (VARS::request("sort") == $field
                        && is_numeric($this->sort[VARS::get("sort")])
                        && $this->sort[VARS::get("sort")] != 0) {
                    //昇順⇔降順
                    $this->sort[$field] = $this->sort[$field] *(-1);
                } elseif (VARS::get("sort") != $field) {
                    unset($this->sort[$field]);
                } else {
                    //昇順
                    $this->sort[$field] = 1;
                }
            }
        }
    }

}
?>
