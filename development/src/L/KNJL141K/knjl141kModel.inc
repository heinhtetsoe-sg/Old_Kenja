<?php

require_once('for_php7.php');

class knjl141kModel extends Model {
    var $examcourseall = array();
    var $examcourse;
    var $cmd;
    var $examyear;

    function knjl141kModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->examcourse   = VARS::post("EXAMCOURSE");
    }

    function getDownloadModel()
    {
        $header = array("ENTERYEAR"          => "年度",
                        "SCHREGNO"           => "学籍番号",
                        "NAME"               => "氏名漢字",
                        "NAME_KANA"          => "氏名かな",
                        "SEX"                => "性別",
                        "COURSECD"           => "課程コード",
                        "MAJORCD"            => "学科コード",
                        "COURSECODE"         => "コースコード",
                        "A_KOKU"             => "国語A",
                        "A_SU"               => "数学A",
                        "A_ENG"              => "英語A",
                        "A_RIKA"             => "理科A",
                        "A_SYA"              => "社会A",
                        "A_TOTAL"            => "合計A",
                        "B_KOKU"             => "国語B",
                        "B_SU"               => "数学B",
                        "B_ENG"              => "英語B",
                        "B_RIKA"             => "理科B",
                        "B_SYA"              => "社会B",
                        "B_TOTAL"            => "合計B"
                        );

        $contents = implode($header, ",")."\n";
        $data = false;

        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knjl141kQuery::selectCvsQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データセット
            $contents .= $row["ENTEXAMYEAR"].","
                        .$row["SCHREGNO"].","
                        .$row["NAME"].","
                        .$row["NAME_KANA"].","
                        .$row["SEX"].","
                        .$row["SUC_COURSECD"].","
                        .$row["SUC_MAJORCD"].","
                        .$row["SUC_COURSECODE"].","
                        .$row["A_KOKU"].","
                        .$row["A_SU"].","
                        .$row["A_ENG"].","
                        .$row["A_RIKA"].","
                        .$row["A_SYA"].","
                        .$row["A_TOTAL"].","
                        .$row["B_KOKU"].","
                        .$row["B_SU"].","
                        .$row["B_ENG"].","
                        .$row["B_RIKA"].","
                        .$row["B_SYA"].","
                        .$row["B_TOTAL"]." \n";
            $data = true;
        }
        $result->free();

        //合格コース名
        if ($this->examcourse != "99999999") {
            $coursename = $db->getOne(knjl141kQuery::getExamCourseName($this));
        } else {
            $coursename = "全コース";
        }
        Query::dbCheckIn($db);

        if($data){
            $filename = $this->examyear."_入学者成績（".$coursename."）.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
?>
