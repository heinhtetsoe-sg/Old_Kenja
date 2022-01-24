<?php

require_once('for_php7.php');

/**
* 定数定義
*/
class knje050Model extends Model {

    var $field    = array();
    var $cmd;
#    var $sort     = array();    //昇順・降順
    var $sort;    //昇順・降順
    var $control;               //コントロールマスタ

    var $schregno;              //学籍番号   SubForm1で使用
    var $name;                  //氏名       SubForm1で使用
    var $year;                  //年度       SubForm1で使用
    var $annual;                //学年(年次) SubForm1で使用


    /**
     * メンバ変数の初期化
     */
    function knje050Model()
    {

        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        //初期画面の設定
//        $this->field["GRADE"]=1;
#        $this->sort["ATTENDNO"]=1;
        $this->sort = 1;

/*        $db = Query::dbCheckOut();
        $query = knje050Query::selectLeftQuery($this);
        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);

        $this->field["COURSECODE"] = $row["COURSECODE"];
        $this->field["COURSENAME"] = $row["NAME"];
        Query::dbCheckIn($db);
*/
    }
    function init()
    {

        $this->cmd          = VARS::request("cmd");

        if (VARS::request("GRADE")){                                  //学年
            $this->field["GRADE"]   = VARS::request("GRADE");
        }
        if (VARS::get("COURSECODE")){                                 //コースコード・コース名
            $this->field["COURSECODE"] = VARS::get("COURSECODE");
            $this->field["COURSENAME"] = VARS::get("COURSENAME");
/*        }else{
            $db = Query::dbCheckOut();
            $query = knje050Query::selectLeftQuery($this);
            $row = $db->getRow($query,DB_FETCHMODE_ASSOC);

            $this->field["COURSECODE"] = $row["COURSECODE"];
            $this->field["COURSENAME"] = $row["NAME"];
            Query::dbCheckIn($db);
*/        }
/*
        if (VARS::get("COURSENAME")){                                 //コース名
            $this->field["COURSENAME"] = VARS::get("COURSENAME");
        }
*/

        if (VARS::request("HR_CLASS")){
            $this->field["HR_CLASS"] = VARS::request("HR_CLASS");      //組
        }
        if (VARS::get("SCHREGNO")){
            $this->field["SCHREGNO"] = VARS::get("SCHREGNO");          //学籍番号
        }
        if (VARS::get("ATTENDNO")){
            $this->field["ATTENDNO"] = VARS::get("ATTENDNO");          //番号
        }
        if (VARS::get("NAME_SHOW")){
            $this->field["NAME_SHOW"] = VARS::get("NAME_SHOW");        //氏名
        }

        $this->field["CHECKD_SCHREGNO"] = VARS::post("SCHREGNO");  //学籍番号

        if(VARS::request("SCHREGNO") !=""){
            $this->schregno        = VARS::request("SCHREGNO");
        }
        if (VARS::get("NAME")){
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("YEAR")){
            $this->year = VARS::get("YEAR");
        }
        if (VARS::post("ANNUAL")){   //学年（年次）
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->field["REPLACE"] = VARS::post("REPLACE");      //評価が１の場合２に置き換える
        //変数をクリア
        if ($this->cmd == 'left'){
            $this->clean();
        }

    }

    function &getMainModel()
    {

        //ソート用 番号、値変更あり
#        $sort = array("ATTENDNO","CHG_GRADES");

#        //ソートが押下
#        if (trim(VARS::request("sort")) != ""){
#            foreach($sort as $field){
#                if (VARS::request("sort") == $field
#                        && is_numeric($this->sort[VARS::get("sort")])
#                        && $this->sort[VARS::get("sort")] != 0) {
#                    //昇順⇔降順
#                    $this->sort[$field] = $this->sort[$field] *(-1);
#                }elseif (VARS::get("sort") != $field) {
#                    unset($this->sort[$field]);
#                }else{
#                    //昇順
#                    $this->sort[$field] = 1;
#                }
#            }
#        }

        //ソート用 番号

        //ソートが押下
        if (trim(VARS::request("sort")) != ""){
            if (is_numeric($this->sort) && $this->sort != 0) {
                //昇順⇔降順
                $this->sort = $this->sort * (-1);
            }else{
                //昇順
                $this->sort = 1;
            }
        }


    }

    /**
     * 変数をクリアします
     */
    function clean()
    {

//      unset($this->field["GRADE"]);       //学年
        unset($this->field["HR_CLASS"]);    //組
        unset($this->field["SCHREGNO"]);    //学籍番号
        unset($this->field["ATTENDNO"]);    //番号
        unset($this->field["NAME_SHOW"]);   //氏名
        unset($this->field["COURSECODE"]);
        unset($this->field["COURSENAME"]);
//      unset($this->field["COURSENAME"]);   //担任
//      unset($this->sort);                  //昇順・降順
    }
    function getDownloadModel()
    {

        if (!is_array($this->field["CHECKD_SCHREGNO"])){
            //データは存在していません。
            $this->setWarning("MSG310");

            return false;
        }
        $header = array("SCHOOLCD"         => "本校区分",
                        "GRADE"            => "学年",
                        "HR_CLASS"         => "組",
                        "ATTENDNO"         => "出席番号",
                        "SCHREGNO"         => "学籍番号",
                        "NAME_SHOW"        => "生徒氏名",
                        "SEXCD"            => "性別区分",
                        "SEXNAME"          => "性別",
                        "YEAR"             => "年度",
                        "RGRADE"           => "履修学年",
                        "COURSECD"         => "課程コード",
                        "MAJORCD"          => "学科コード",
                        "COURSECODE"       => "コースコード",
                        "COURSECODENAME"   => "コースコード名",
                        "CLASSCD"          => "教科コード",
                        "CLASSNAME"        => "教科名",
                        "CLASSABBV"        => "教科略称",
                        "CLASSNAME_ENG"    => "教科名英字",
                        "CLASSABBV_ENG"    => "教科略称英字",
                        "SUBCLASSCD"       => "科目コード",
                        "SUBCLASSNAME"     => "科目名",
                        "SUBCLASSABBV"     => "科目略称",
                        "SUBCLASSNAME_ENG" => "科目名英字",
                        "SUBCLASSABBV_ENG" => "科目略称英字",
                        "GRAD_VALUE"        => "評価",
                        "CREDITS"          => "単位数",
                        "ADDCREDITCD"      => "増加区分",
                        "REMARK"           => "履修科目備考");

        $db = Query::dbCheckOut();


        //SQL文発行
        $query = knje050Query::selectCvsQuery($this);
        $result = $db->query($query);
        $contents = implode($header, ",")."\n";
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $filename = sprintf("%d学年%s.csv",
                                         $this->field["GRADE"],
                                         $this->field["COURSENAME"]
                                );
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            //データは存在していません。
            $this->setWarning("MSG303");

            return false;
        }
    }
}
//var_dump($_REQUEST);
?>
