<?php

require_once('for_php7.php');

class knja262Model extends Model {
    var $examcourseall = array();
    var $grade;
    var $cmd;
    var $examyear;

    function knja262Model()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->cmd     = VARS::request("cmd");
        $this->grade   = VARS::post("GRADE");
        //プロパティファイル
        $this->getPropertiesAll();
    }

    function getDownloadModel()
    {
        $header = array("YEAR"          => "年度",
                        "SCHREGNO"      => "学籍番号",
                        "NAME"          => "氏名漢字",
                        "NAME_KANA"     => "氏名かな",
                        "SEX"           => "性別",
                        "GRADE"         => "年",
                        "HR_CLASS"      => "組",
                        "ATTENDNO"      => "番",
                        "COURSECD"      => "課程コード",
                        "MAJORCD"       => "学科コード",
                        "COURSECODE"    => "コースコード",
                        "AVG_ASSEC"     => "評定平均",
                        "AVG_RECORD"    => "成績平均"
                        );

        $contents = implode($header, ",")."\n";
        $data = false;

        $db = Query::dbCheckOut();

        //SQL文発行
        $query = knja262Query::selectCvsQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

			$asses = "";
			$record = "";
			if ($row["LEVEL_CNT"] > 0){
				$asses = round((int)$row["LEVEL_SUM"]/(int)$row["LEVEL_CNT"],1);
			}
			if ($row["SEM_CNT"] > 0){
				$record = round((int)$row["SEM_SUM"]/(int)$row["SEM_CNT"],1);
			}
            //データセット
            $contents .= $this->examyear.","
                        .$row["SCHREGNO"].","
                        .$row["NAME"].","
                        .$row["NAME_KANA"].","
                        .$row["SEX"].","
                        .$row["GRADE"].","
                        .$row["HR_CLASS"].","
                        .$row["ATTENDNO"].","
                        .$row["COURSECD"].","
                        .$row["MAJORCD"].","
                        .$row["COURSECODE"].","
                        .$asses.","
                        .$record." \n";
            $data = true;
        }
        $result->free();

        //合格コース名
        if ($this->grade != "01") {
            $coursename = "2年生";
        } else {
            $coursename = "1年生";
        }
        Query::dbCheckIn($db);

        if($data){
            $filename = $this->examyear."_進級者成績（".$coursename."）.csv";
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
