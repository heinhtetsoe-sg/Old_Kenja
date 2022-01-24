<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja260kModel extends Model {
    var $field      = array();
    var $select_data      = array();
    var $opt_subclass      = array();
    var $cmd;
    var $jhflg;     //NO001
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knja260kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"             =>  VARS::post("YEAR"),         //年度
                            "GAKKI"             =>  VARS::post("GAKKI"),        //学期
                            "GAKKI_MAX"         =>  VARS::post("GAKKI_MAX"),    //最終学期
                            "TESTKINDCD"        =>  VARS::post("TESTKINDCD"),   //テストコード
                            "DBNAME"            =>  VARS::post("DBNAME")        //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->getPropertiesAll();
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    /**
     * CSV（学年評定以外）
     */
    function getDownloadModel()
    {
        //テスト//////////////////////////////////////////////////////////
        //ヘッダの作成//
        $header = array("YEAR"         => "年度",
                        "SEMESTER"     => "学期",
                        "SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "NAME_SHOW"    => "生徒氏名",
                        "TESTITEMNAME" => "テスト種別名",
                        );
        $subheader = array("YEAR"         => "",
                           "SEMESTER"     => "",
                           "SCHREGNO"     => "",
                           "GRADE"        => "",
                           "HR_CLASS"     => "",
                           "ATTENDNO"     => "",
                           "NAME_SHOW"    => "",
                           "TESTITEMNAME" => "",
                           );

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行（科目コード・科目名の取得）
        $query = knja260kQuery::getSelectSubClass1($this,$this->field["GAKKI"]);
        $result = $db->query($query);
        $contents = implode($header, ",") .",";
        $subcontents = implode($subheader,",").",";
        $this->opt_subclass = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //NO001 NO002
            if ($this->jhflg != "on" && $this->field["GAKKI"]=="9"){
                $contents .= $row["SUBCLASSNAME"].","."フラグ,"."出欠情報,"."評定,"."5段階,"."10段階,";
                $subcontents .= $row["SUBCLASSCD"].",".",".",".",".",".",";
            }else {
                $contents .= $row["SUBCLASSNAME"].","."フラグ,"."出欠情報,";
                $subcontents .= $row["SUBCLASSCD"].",".",".",";
            }
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->opt_subclass[] = $row["CLASSCD"].'_'.$row["SCHOOL_KIND"].'_'.$row["CURRICULUM_CD"].'_'.$row["SUBCLASSCD"];
            } else {
                $this->opt_subclass[] = $row["SUBCLASSCD"];
            }
        }
        $contents .= "\n";
        $contents .= $subcontents;
        $contents .= "\n";
        //出力データの作成//
        $gakkimei = array();
        $query = knja260kQuery::getSelectSeme($this->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $gakkimei[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }
//        for($i=0;$i<(int)$this->control["学期数"];$i++){
//            $gakkimei[$i+1] = $this->control["学期名"][$i+1];
//           }
        //SQL文発行
        $testname = "";
        if ($this->field["TESTKINDCD"] == "01"){
            $testname = "中間試験";
        }else if ($this->field["TESTKINDCD"] == "02"){
            $testname = "期末試験";
        }else if ($this->field["TESTKINDCD"] == "0"){
            $testname = "学期成績";
        }else {
            $testname = "学年成績";
        }
        $query = knja260kQuery::selectCsvQuery1($this,$testname);
        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $contents .= $this->field["YEAR"].",";
            $contents .= $gakkimei[$this->field["GAKKI"]].",";
            for($i=0;$i<get_count($this->opt_subclass);$i++){
                if ($row["DI".$i] == "1"){
                    $row["DI".$i] = "KK";
                }else if($row["DI".$i] == "2"){
                    $row["DI".$i] = "KS";
                }else {
                    $row["DI".$i] = "";
                }
            }
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            $couse="テスト毎";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }
    /**
     * CSV（学年評定）---2005.09.30
     */
    function getDownloadModel2()
    {
        //ヘッダの作成//
        $header = array("YEAR"         => "年度",
                        "SEMESTER"     => "学期",
                        "SCHREGNO"     => "学籍番号",
                        "GRADE"        => "学年",
                        "HR_CLASS"     => "組",
                        "ATTENDNO"     => "出席番号",
                        "NAME_SHOW"    => "生徒氏名",
                        "TESTITEMNAME" => "テスト種別名",
                        );
        $subheader = array("YEAR"         => "",
                           "SEMESTER"     => "",
                           "SCHREGNO"     => "",
                           "GRADE"        => "",
                           "HR_CLASS"     => "",
                           "ATTENDNO"     => "",
                           "NAME_SHOW"    => "",
                           "TESTITEMNAME" => "",
                           );

        //DB接続
        $db = Query::dbCheckOut();

        $query = knja260kQuery::selectReplaceSubclass($this);
        $result = $db->query($query);
        $combinedSubclasscd = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($this->Properties["useCurriculumcd"] == '1') {
                $combinedSubclasscd[$row["COMBINED_SUBCLASSCD"]][] = $row["CLASSCD"].'_'.$row["SCHOOL_KIND"].'_'.$row["CURRICULUM_CD"].'_'.$row["SUBCLASSCD"];
            } else {
                $combinedSubclasscd[$row["COMBINED_SUBCLASSCD"]][] = $row["SUBCLASSCD"];
            }
        }

        //科目コード・科目名の取得
        $query = knja260kQuery::getSelectSubClass2($this);
        $result = $db->query($query);
        $contents = implode($header, ",") .",";
        $subcontents = implode($subheader,",").",";
        $this->opt_subclass = array();

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $contents .= $row["SUBCLASSNAME"].","."フラグ,"."出欠情報,";
            $subcontents .= $row["SUBCLASSCD"].",".",".",";
            if ($this->Properties["useCurriculumcd"] == '1') {
                $this->opt_subclass[] = $row["CLASSCD"].'_'.$row["SCHOOL_KIND"].'_'.$row["CURRICULUM_CD"].'_'.$row["SUBCLASSCD"];
            } else {
                $this->opt_subclass[] = $row["SUBCLASSCD"];
            }
        }
        $contents .= "\n";
        $contents .= $subcontents;
        $contents .= "\n";
        //出力データの作成//
        $gakkimei = array();
        $query = knja260kQuery::getSelectSeme($this->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $gakkimei[$row["SEMESTER"]] = $row["SEMESTERNAME"];
        }
        //SQL文発行
        if ($this->field["TESTKINDCD"] == "99") $query = knja260kQuery::selectCsvQuery2($this);
        if ($this->field["TESTKINDCD"] == "88") $query = knja260kQuery::selectCsvQuery3($this);

        $result = $db->query($query);
        $data = false;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            if ($this->field["TESTKINDCD"] == "99") {
                foreach ($combinedSubclasscd as $combinedSub => $subCdArray) {
                    $avg = 0;
                    $total = 0;
                    foreach ($subCdArray as $key => $subCd) {
                        $total = (int)$total + (int)$row["ASSES".$subCd];
                    }
                    $avg = round((int)$total / get_count($subCdArray) * 100, -2) / 100;
                    $row["ASSES".$combinedSub] = $avg;
                }
            }
            $contents .= $this->field["YEAR"] . ",";
            $contents .= $gakkimei[$this->field["GAKKI"]] . ",";
            $contents .= implode($row, ",") ."\n";
            $data = true;
        }

        $result->free();
        Query::dbCheckIn($db);

        if ($data){
            if ($this->field["TESTKINDCD"] == "99") {
                if ($this->field["GAKKI"] == 9) {
                    $couse="学年評定";
                } else {
                    $couse="学年評定(" . $gakkimei[$this->field["GAKKI"]] . "末時点)";
                }
            }
            if ($this->field["TESTKINDCD"] == "88") $couse="学期評定(" . $gakkimei[$this->field["GAKKI"]] . "のみ)";
            $filename = sprintf("%s.csv",$couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }
}
?>
