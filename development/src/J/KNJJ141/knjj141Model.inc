<?php

require_once('for_php7.php');
class knjj141Model extends Model {
    var $field = array();
    var $cmd;
    var $selectSchoolKindSql;

    function knjj141Model() {
    }

    function init() {
        $this->field = array("BASE_DATE"    => VARS::post("BASE_DATE"),
                             "GRADE"        => VARS::post("GRADE"),
                             "PRTPATTERN"   => VARS::post("PRTPATTERN")
                            );

        $this->cmd   = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
            $this->selectSchoolKindSql = "('".str_replace(":", "','", $this->selectSchoolKind)."')";
        }
    }

    //変数をクリアします
    function clean() {
    }

    //ＣＳＶ出力
    function getDownloadModel() {
        $contents = "";
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル行
        $wrk_nendo = common::DateConv1(str_replace("-","/",CTRL_DATE),10);
        $title = $this->field["PRTPATTERN"] == "2" ? "学年別・男女別集計表" : "部活動人数一覧表";
        $wrk_date = common::DateConv1(str_replace("-","/",$this->field["BASE_DATE"]),0);

        $data = false;
        $dataTemp = array();
        $header = array();

        $schArry = array();
        $query = knjj141Query::getRegdHdat($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schArry[] = $row;
        }

        $syukeidat = array();
        $query = knjj141Query::getClubDataSql($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sKey1 = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["SCHOOLCD"]."-".$row["SCHOOL_KIND"]."-".$row["SEX"];
            $sKey2 = $row["CLUBCD"];
            if (!array_key_exists($sKey1, $syukeidat)) {
                $syukeidat[$sKey1] = array();
            }
            $syukeidat[$sKey1][$sKey2] = $row["CNT"];
        }

        $befSchKind = "";
        $befGrade = "";
        $befSch = array();
        $titledelim = "";
        $schCnt = 0;

        foreach ($schArry as $sch) { //クラス
            $schCnt++;
            if ($befSchKind != $sch["SCHOOL_KIND"]) {  //$schArryにデータがあれば、最初に必ず入るはず。
                $clubArryWk = array();
                $header = array();
                $query = knjj141Query::getClubSql($this);
                $result = $db->query($query);
                $schKChkBk = "";
                $ttlPrtFlg = false;
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $findMatchFlg = false;
                    if ($sch["SCHOOL_KIND"] == $row["SCHOOL_KIND"]) {  //対象校種のクラブデータだけを利用。
                        $findMatchFlg = true;
                    }
                    //校種の切り替わりで合計を設定(学年指定なら、ここでは入らなくて良い=ALLだけが対象)。
                    //ただし、一度部活と一致データを見つけている事。でないと、部活の高→中遷移時に計を出して、中の最後に計が出なくなる。
                    if ($schKChkBk != "" && $schKChkBk != $row["SCHOOL_KIND"] && "ALL" == $this->field["GRADE"] && ($sch["SCHOOL_KIND"] != $schKChkBk && !$findMatchFlg)){
                        $ttlPrtFlg = true;
                        $header[] = "計";
                        $caWk = array("SCHOOLCD"=>$row["SCHOOLCD"], "CLUBCD"=>"ZZZZ");  //校種切り替わりで"計"を追加
                        $clubArryWk[] = $caWk;
                    }
                    if ($this->Properties["useClubMultiSchoolKind"] == "1") {
                        //校種は関係ないはずなので、そのまま設定
                        $header[] = $row["CLUBNAME"];
                        $clubArryWk[] = $row;
                    } else {
                        if ($sch["SCHOOL_KIND"] == $row["SCHOOL_KIND"]) {  //対象校種のクラブデータだけを利用。
                            $header[] = $row["CLUBNAME"];
                            $clubArryWk[] = $row;
                        }
                    }
                    $schKChkBk = $row["SCHOOL_KIND"];
                }

                if (!$ttlPrtFlg || ($ttlPrtFlg && "ALL" == $this->field["GRADE"] &&get_count($schArry) == $schCnt)) {  //計を出力していない＝校種が変わらない学校(つまり中学だけ、高校だけの学校)と全ての"最後"をここでフォロー
                    $header[] = "計";
                    $caWk = array("SCHOOLCD"=>$row["SCHOOLCD"], "CLUBCD"=>"ZZZZ");  //校種切り替わりで"計"を追加
                    $clubArryWk[] = $caWk;
                }
            }
            for ($sx = 1;$sx <= 2;$sx++) {  //性別
                if ($sx == 1 && ($befGrade != "" && $befGrade != $sch["GRADE"])) {  //切り替わり直後に学年の切り替わりなら、"学年計を出力"
                    $contents .= $this->prtTotalLine($clubArry, $syukeidat, $befSch, $befSch["SCHOOL_KIND"], $sch["SCHOOL_KIND"]);
                }
                if ($sx == 1 && $befSchKind != $sch["SCHOOL_KIND"]) {
                    $contents .= "{$titledelim}{$wrk_nendo}　{$title},,,,{$wrk_date}\n";
                    $titledelim = "\n";
                    $contents .= "年組,＼部活動名,".implode($header, ",") ."\n";
                }
                //合計出力をしてから、クラブリストを更新する(でないと、高校->中学切り替わった状態で合計出力される)
                $clubArry = $clubArryWk;
                $lineBuf = $sch["HR_NAME"];
                $lineBuf .= ($sx == 1 ? ",男" : ($sx == 2 ? ",女" : ",計"));
                $lineSub = "";
                $lineSub .= $this->prtLine($clubArry, $syukeidat, $sch["GRADE"], $sch["HR_CLASS"], $sch["SCHOOL_KIND"], $sx);
                if ($lineSub != "") {
                    $data = true;
                    $lineBuf .= $lineSub;
                }
                if ($this->field["PRTPATTERN"] != "2") {
                    $contents .= $lineBuf."\n";
                }
            }
            $befSchKind = $sch["SCHOOL_KIND"];
            $befGrade = $sch["GRADE"];
            $befSch = $sch;
        }
        if ($data) {
            //最後の合計部分を出力する
            $contents .= $this->prtTotalLine($clubArry, $syukeidat, $befSch, $befSch["SCHOOL_KIND"], "");
        }

        Query::dbCheckIn($db);
        if ($data) {
            $couse = "{$wrk_nendo}　{$title}";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    function prtTotalLine($clubArry, $syukeidat, $befSch, $befSchKind, $nowSchKind) {
        $lineRet = "";
        for ($wsx = 1;$wsx <= 3;$wsx++) {  //性別
            $lineRet .= $befSch["GRADE"]."学年合計";
            $lineRet .= ($wsx == 1 ? ",男" : ($wsx == 2 ? ",女" : ",計"));
            $lineRet .= $this->prtLine($clubArry, $syukeidat, $befSch["GRADE"], "999", $befSchKind, $wsx);
            $lineRet .= "\n";
        }
        if ($befSchKind != $nowSchKind && "ALL" == $this->field["GRADE"]) {
            //(校種別男女計及び総計を出力)
            for ($wsx = 1;$wsx <= 3;$wsx++) {  //性別
                $lineBuf = "";
                $lineBuf .= "全学年合計";
                $lineBuf .= ($wsx == 1 ? ",男" : ($wsx == 2 ? ",女" : ",計"));
                $lineBuf .= $this->prtLine($clubArry, $syukeidat, "99", "999", $befSchKind, $wsx);

                $lineRet .= $lineBuf."\n";
            }
        }
        return $lineRet;
    }

    function prtLine($clubArry, $syukeidat, $schGrade, $schHr, $schKind, $sx) {
        $lineRet = "";
        $ttlCnt = 0;
        foreach ($clubArry as $ccd) {  //クラブ
            $sKey1 = $schGrade."-".$schHr."-".$ccd["SCHOOLCD"]."-".$schKind."-".$sx;
            $sKey2 = $ccd["CLUBCD"];
            if ($ccd["CLUBCD"] == "ZZZZ") {  //計のデータなら、計を出力
                $lineRet .= ",".$ttlCnt;
            } else if (!array_key_exists($sKey1, $syukeidat) || !array_key_exists($sKey2, $syukeidat[$sKey1])) {
                $lineRet .= ",0";
            } else {
                $lineRet .= ",".$syukeidat[$sKey1][$sKey2];
                $ttlCnt += $syukeidat[$sKey1][$sKey2];
            }
        }
        return $lineRet;
    }
}
?>
