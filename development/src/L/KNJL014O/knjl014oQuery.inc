<?php

require_once('for_php7.php');

class knjl014oquery extends Query
{
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS label, ";
        $query .= "     NAMECD2 AS value, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分マスタ
    public function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";                 //NAMECD2
        $query .= "     TESTDIV || ':' || NAME AS LABEL ";   //NAME1
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $not = ($model->field["TESTDIV"] == '1') ? ' NOT ': '';

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_MIRAI_OMIYA_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";

        $db->query($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //ENTEXAM_MIRAI_OMIYA_DAT
            $data = array();
            foreach ($data_arr[$i] as $key => $val) {
                $data[$key][TEXT]           = $data_arr[$i][$key];
            }
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";
            $query = Query::insertSQL($data, "ENTEXAM_MIRAI_OMIYA_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    public function getCkipCsv($model)
    {
        $query .= " WITH GET_PRISCHOOL_DATA_TBL AS ( ";
        $query .= " SELECT ";
        $query .= "   T1.ENTEXAMYEAR, ";
        $query .= "   T1.APPLICANTDIV, ";
        $query .= "   T1.TEST_NAME_ABBV AS TESTDIV_NA, ";
        $query .= "   T1.EXAMNO, ";
        $query .= "   CASE WHEN POSSTR(T1.PRISCHOOL_NAME, '：') > 0 THEN SUBSTR(T1.PRISCHOOL_NAME, 1, POSSTR(T1.PRISCHOOL_NAME, '：')) ELSE T1.PRISCHOOL_NAME END AS PRISCH_NAME, ";
        $query .= "   CASE WHEN POSSTR(T1.PRISCHOOL_NAME, '：') > 0 THEN SUBSTR(T1.PRISCHOOL_NAME, POSSTR(T1.PRISCHOOL_NAME, '：')+3) ELSE T1.PRISCHOOL_NAME END AS PRISCH_CLSNAME ";
        $query .= " FROM ";
        $query .= "  ENTEXAM_MIRAI_OMIYA_DAT T1 ";
        $query .= " ), GET_PRISCHOOL_CONNECT AS ( ";
        $query .= " SELECT ";
        $query .= "  GPDT.*, ";
        $query .= "  PY.PRISCHOOLCD ";
        $query .= " FROM ";
        $query .= "  GET_PRISCHOOL_DATA_TBL GPDT ";
        $query .= "  LEFT JOIN PRISCHOOL_MST PM ";
        $query .= "    ON PM.PRISCHOOL_NAME = GPDT.PRISCH_NAME ";
        $query .= "  LEFT JOIN PRISCHOOL_YDAT PY ";
        $query .= "    ON PY.YEAR = GPDT.ENTEXAMYEAR ";
        $query .= "   AND PY.PRISCHOOLCD = PM.PRISCHOOLCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= "   , T1.TEST_NAME_ABBV AS TESTDIV_NA ";
        $query .= "   , L007.NAMECD2 AS ERACD ";
        $query .= "   , INT(SUBSTR(T1.BIRTHDAY, 1,4)) - INT(L007.NAMESPARE1) + 1 AS BIRTH_Y ";
        $query .= "   , SUBSTR(T1.BIRTHDAY, 5,2) AS BIRTH_M ";
        $query .= "   , SUBSTR(T1.BIRTHDAY, 7,2) AS BIRTH_D ";
        $query .= "   , GPC.PRISCHOOLCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MIRAI_OMIYA_DAT T1 ";
        $query .= " LEFT JOIN NAME_MST L007 ON L007.NAMECD1 = 'L007' ";
        $query .= "  AND DATE(SUBSTR(T1.BIRTHDAY, 1,4) || '-' || SUBSTR(T1.BIRTHDAY, 5,2) || '-' || SUBSTR(T1.BIRTHDAY, 7,2)) BETWEEN DATE(REPLACE(L007.NAMESPARE2, '/', '-')) AND DATE(REPLACE(L007.NAMESPARE3, '/', '-')) ";
        $query .= " LEFT JOIN GET_PRISCHOOL_CONNECT GPC ";
        $query .= "   ON GPC.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "  AND GPC.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "  AND GPC.TESTDIV_NA = T1.TEST_NAME_ABBV ";
        $query .= "  AND GPC.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        //'10:中高一貫部入試'のみを対象として、内部で'1'に変換するため、条件指定を変更。
        $query .= "     AND T1.APPLICANTDIV Like '10%' ";
        $query .= "     AND T1.TEST_NAME_ABBV = '{$model->field["TESTDIV"]}' ";

        return $query;
    }

    //カウント取得
    public function getCntData($table, $year, $examNo, $applicantDiv='', $seq='')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR   = '{$year}' ";
        $query .= "     AND EXAMNO        = '{$examNo}' ";
        if ($applicantDiv != '') {
            $query .= " AND APPLICANTDIV  = '1' ";
        }
        if ($seq != '') {
            $query .= " AND SEQ           = '{$seq}' ";
        }

        return $query;
    }

    //データ取得
    public function getTblData($year, $examNo, $applicantDiv='', $seq='')
    {
        $query  = " SELECT ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR   = '{$year}' ";
        $query .= "     AND EXAMNO        = '{$examNo}' ";
        if ($applicantDiv != '') {
            $query .= " AND APPLICANTDIV  = '1' ";
        }
        if ($seq != '') {
            $query .= " AND SEQ           = '{$seq}' ";
        }

        return $query;
    }

    //PRISCHOOLCD取得
    public function getPriCd($priName)
    {
        $query  = " SELECT ";
        $query .= "   PRISCHOOLCD ";
        $query .= " FROM ";
        $query .= "   PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   PRISCHOOL_NAME = '{$priName}' ";
        return $query;
    }

    //更新
    public function updateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $fsAreaCdHenkanArr = array();
        $query = knjl014oQuery::getNameMst($model, 'Z003');
        $result = $db->query($query);
        while ($rowPref = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $fsAreaCdHenkanArr[$rowPref["NAME1"]] = $rowPref["VALUE"];
        }

        $query = knjl014oQuery::getCkipCsv($model);
        $result = $db->query($query);
        $ercnt = 0;
        $cnt = 0;
        while ($rowCkip = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!knjl014oquery::updateCheckData($rowCkip)) {
                $ercnt++;
                continue;
            }
            //件数取得
            $baseCnt = $db->getOne(knjl014oQuery::getCntData('ENTEXAM_APPLICANTBASE_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["EXAMNO"], $rowCkip["APPLICANTDIV"]));
            $recTblDat = array();
            if ($baseCnt > 0) {
                //登録データがあれば、既存登録されているデータ(TESTDIVを利用)を取得。
                //登録は、getRowの添え字が数値なので、取得値に注意。(0:年度、1:APPLICANTDIV、2:TESTDIV、3:EXAMNO)
                $recTblDat = $db->getRow(knjl014oQuery::getTblData($rowCkip["ENTEXAMYEAR"], $rowCkip["EXAMNO"], $rowCkip["APPLICANTDIV"]));
            }
            $addrCnt = $db->getOne(knjl014oQuery::getCntData('ENTEXAM_APPLICANTADDR_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["EXAMNO"]));
            $s005Cnt = $db->getOne(knjl014oQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["EXAMNO"], "", '005'));
            $s008Cnt = $db->getOne(knjl014oQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["EXAMNO"], "", '008'));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]        = $rowCkip["ENTEXAMYEAR"];
            //APPLICANTDIVは固定で取得するため、"1"固定とする(余計な日本語も付いているため)。
            $data["APPLICANTDIV"][TEXT]       = "1";
            $data["EXAMNO"][TEXT]             = $rowCkip["EXAMNO"];
            //登録されているTESTDIVよりも登録するTESTDIV_NAが小さい場合のみ、更新
            //$recTblDatの添え字は、getRowのため。詳細は、取得元のコメントを参照。
            $data["TESTDIV"][TEXT]            = (get_count($recTblDat) == 0  || $recTblDat[2] > $rowCkip["TESTDIV_NA"]) ? $rowCkip["TESTDIV_NA"] : $recTblDat[2];
            //TESTDIV?は"指示画面の設定値"を設定
            $data["TESTDIV".$rowCkip["TESTDIV_NA"]][TEXT]           = $rowCkip["TESTDIV_NA"];
            $data["SHDIV"][TEXT]              = "1"; //1固定
            $data["DESIREDIV"][TEXT]          = "1"; //1固定
            //性別は全角文字のため、変換
            $data["SEX"][TEXT]                = $rowCkip["SEX"] == "男" ? "1" : "2";
            if ($rowCkip["BIRTHDAY"]) {
                $data["ERACD"][VARCHAR]       = $rowCkip["ERACD"];
                $data["BIRTH_Y"][VARCHAR]     = $rowCkip["BIRTH_Y"];
                $data["BIRTH_M"][VARCHAR]     = $rowCkip["BIRTH_M"];
                $data["BIRTH_D"][VARCHAR]     = $rowCkip["BIRTH_D"];
            }
            $data["BIRTHDAY"][DATE]           = substr($rowCkip["BIRTHDAY"], 0, 4)."-".substr($rowCkip["BIRTHDAY"], 4, 2)."-".substr($rowCkip["BIRTHDAY"], 6, 2);
            //出身学校がNULLなら、NULLで設定
            $data["FS_CD"][TEXT]              = $rowCkip["FS_CD"] == "" ? "" : substr('0000000'.$rowCkip["FS_CD"], -7);
            $setAreaName = str_replace(array('都', '道', '府', '県'), '', $rowCkip["ADDRESS1"]);
            $data["FS_AREA_CD"][TEXT]         = ($fsAreaCdHenkanArr[$setAreaName] != '') ? $fsAreaCdHenkanArr[$setAreaName]: '08'; //08:その他
            $data["FS_GRDYEAR"][TEXT]         = CTRL_YEAR+1;
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";

            if ($baseCnt == 0) {
                $data["NAME"][TEXT]               = $rowCkip["NAME_SEI"]."　".$rowCkip["NAME_MEI"];
                $data["NAME_KANA"][TEXT]          = $rowCkip["NAME_KANA_SEI"]."　".$rowCkip["NAME_KANA_MEI"];
                $data["FS_NAME"][TEXT]            = mb_strcut($rowCkip["FS_NAME_ETC"] ? $rowCkip["FS_NAME_ETC"] : $rowCkip["FS_NAME"], 0, 75);
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '1'";
                $where .= "   AND EXAMNO        = '".$rowCkip["EXAMNO"]."'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["EXAMNO"][TEXT]           = $rowCkip["EXAMNO"];
            //郵便番号に'-'を付加
            $data["ZIPCD"][TEXT]            = $rowCkip["ZIPCD"] == "" ? "" : substr($rowCkip["ZIPCD"], 0, 3) . "-" . substr($rowCkip["ZIPCD"], 3);
            $data["ADDRESS1"][TEXT]         = $rowCkip["ADDRESS1"].$rowCkip["ADDRESS2"].$rowCkip["ADDRESS3"];
            $data["ADDRESS2"][TEXT]         = $rowCkip["ADDRESS4"];
            $data["TELNO"][TEXT]            = $rowCkip["TELNO"];
            //保護者の郵便番号は、生徒の物をコピー
            $data["GZIPCD"][TEXT]           = $data["ZIPCD"][TEXT];
            $data["GADDRESS1"][TEXT]        = $rowCkip["ADDRESS1"].$rowCkip["ADDRESS2"].$rowCkip["ADDRESS3"];
            $data["GADDRESS2"][TEXT]        = $rowCkip["ADDRESS4"];
            $data["GTELNO"][TEXT]           = $rowCkip["GTELNO"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if ($addrCnt == 0) {
                $data["GNAME"][TEXT]            = $rowCkip["GNAME_SEI"]."　".$rowCkip["GNAME_MEI"];
                $data["GKANA"][TEXT]            = $rowCkip["GKANA_SEI"]."　".$rowCkip["GKANA_MEI"];
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND EXAMNO        = '".$rowCkip["EXAMNO"]."'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(005)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["EXAMNO"][TEXT]           = $rowCkip["EXAMNO"];
            $data["SEQ"][TEXT]              = '005';
            if ($rowCkip["SHIBOU_NAME1"] == "" || $rowCkip["SHIBOU_NAME1"] == "大宮開成中学校") {
                $data["REMARK1"][TEXT] = "1";
            }
            $data["REMARK2"][TEXT] = $rowCkip["BRO_SCH_NAME"];
            $data["REMARK3"][TEXT] = $rowCkip["BRO_SCH_GRADE"];
            if (preg_replace('/.*[0-9０１２３４５６７８９][.．]大宮開成/', '', $data["REMARK3"][TEXT]) != $data["REMARK3"][TEXT]) {
                // 「*２．大宮開成高校２年生」や「*４．大宮開成中学校３年生」から「*X.大宮開成」をカット
                $data["REMARK3"][TEXT] = preg_replace('/.*[0-9０１２３４５６７８９][.．]大宮開成/', '', $data["REMARK3"][TEXT]);
            }
            $setRemark4 = "";
            $setRemarkSep = "";
            $setRemarkCnt = 0;
            if ($rowCkip["SHIBOU_NAME1"] != "" && $rowCkip["SHIBOU_NAME1"] != "大宮開成中学校") {
                $setRemark4 .= $rowCkip["SHIBOU_NAME1"];
                $setRemarkSep = "\n";
                $setRemarkCnt++;
            }
            if ($rowCkip["SHIBOU_NAME2"] != "" && $rowCkip["SHIBOU_NAME2"] != "大宮開成中学校") {
                $setRemark4 .= $setRemarkSep.$rowCkip["SHIBOU_NAME2"];
                $setRemarkSep = "\n";
                $setRemarkCnt++;
            }
            if ($setRemarkCnt < 2 && $rowCkip["SHIBOU_NAME3"] != "" && $rowCkip["SHIBOU_NAME3"] != "大宮開成中学校") {
                $setRemark4 .= $setRemarkSep.$rowCkip["SHIBOU_NAME3"];
            }
            $data["REMARK4"][TEXT] = $setRemark4;
            $data["REGISTERCD"][TEXT] = STAFFCD;
            $data["UPDATED"][FUNC] = "CURRENT TIMESTAMP";

            if ($s005Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND EXAMNO        = '".$rowCkip["EXAMNO"]."'";
                $where .= "   AND SEQ           = '005'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(008)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["EXAMNO"][TEXT]           = $rowCkip["EXAMNO"];
            $data["SEQ"][TEXT]              = '008';
            $cpos = mb_strpos($rowCkip["PRISCHOOL_NAME"], "：", 0, "UTF-8");
            if ($rowCkip["PRISCHOOL_NAME"] != "") {
                $chkPriStr = "";
                $setRemark2 = "";
                if (mb_substr($rowCkip["PRISCHOOL_NAME"], 0, $cpos) != "") {
                    $chkPriStr = mb_substr($rowCkip["PRISCHOOL_NAME"], 0, $cpos);
                    $setRemark2 = mb_substr($rowCkip["PRISCHOOL_NAME"], $cpos+1);
                } else {
                    //"："が無いがNULLではないので、文字列を"塾名称"として処理。REMARK2は空文字とする。
                    $chkPriStr = $rowCkip["PRISCHOOL_NAME"];
                    $setRemark2 = "";
                }
                $queryPcd = "";
                $queryPcd = knjl014oQuery::getPriCd($chkPriStr);
                $r1val = $db->getOne($queryPcd);
                //塾マスタを検索した結果、塾コードがない場合、塾コードに「0000999」を登録・・・※その他塾
                //塾マスタを検索した結果、塾コードがない場合、教室名にCSV塾名称をそのまま登録
                $data["REMARK1"][TEXT] = $r1val == "" ? "0000999" : $r1val;
                $data["REMARK2"][TEXT] = $r1val == "" ? $rowCkip["PRISCHOOL_NAME"] : $setRemark2;
            } else {
                $data["REMARK1"][TEXT] = "";
                $data["REMARK2"][TEXT] = "";
            }

            if ($s008Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND EXAMNO        = '".$rowCkip["EXAMNO"]."'";
                $where .= "   AND SEQ           = '008'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);

        return "登録：{$cnt}件、エラー：{$ercnt}件";
    }

    public function updateCheckData($rowCkip)
    {
        //登録先テーブルに登録する桁数内か、チェックする。
        $lentbl = array(
            "ENTEXAMYEAR"  => 4,
            "APPLICANTDIV" => 1,     //内部でskip
            "TESTDIV"      => 1,
            "EXAMNO"       => 5,
            "ADDRESS1"     => 150,
            "ADDRESS2"     => 150,
            "GADDRESS1"    => 150,     //内部でskip
            "GADDRESS2"    => 150,     //内部でskip
            "BIRTHDAY"     => 10,     //文字列の状態なので、DBとは違う長さ
            "BIRTH_D"      => 2,
            "BIRTH_M"      => 2,
            "BIRTH_Y"      => 2,
            "GKANA"        => 120,
            "GNAME"        => 60,
            "GTELNO"       => 14,
            "GZIPCD"       => 8,
            "NAME"         => 60,
            "NAME_KANA"    => 120,
            "SEX"          => 3,       //性別を1文字で入れているので、3byteでチェックし、後で変換する。
            "TELNO"        => 14,
            "ZIPCD"        => 8
            );
        $chkflg = true;
        //(DBのデータにはチェック不要のものもある為)チェック項目ベースのチェックをする。
        foreach ($lentbl as $key => $val) {
            if ("ADDRESS1" == $key) {
                $lenchk = strlen($rowCkip["ADDRESS1"].$rowCkip["ADDRESS2"].$rowCkip["ADDRESS3"]);
                if ($lenchk > $lentbl[$key]) {
                    $chkflg = false;
                }
            } elseif ("ADDRESS2" == $key) {
                if (strlen($rowCkip["ADDRESS4"]) > $lentbl[$key]) {
                    $chkflg = false;
                }
            } elseif ("GADDRESS1" == $key || "GADDRESS2" == $key) {
                //ADDRESS1,2と同じ値になるため、チェック不要としてskip。
            } elseif ("APPLICANTDIV" == $key) {
                //APPLICANTDIVは固定値のため、チェック不要としてskip。
            } elseif ("GKANA" == $key) {
                $lenchk = strlen($rowCkip["GKANA_SEI"]."　".$rowCkip["GKANA_MEI"]);
                if ($lenchk > $lentbl[$key]) {
                    $chkflg = false;
                }
            } elseif ("GNAME" == $key) {
                $lenchk = strlen($rowCkip["GNAME_SEI"]."　".$rowCkip["GNAME_MEI"]);
                if ($lenchk > $lentbl[$key]) {
                    $chkflg = false;
                }
            } elseif ("NAME" == $key) {
                $lenchk = strlen($rowCkip["NAME_SEI"]."　".$rowCkip["NAME_MEI"]);
                if ($lenchk > $lentbl[$key]) {
                    $chkflg = false;
                }
            } elseif ("NAME_KANA" == $key) {
                $lenchk = strlen($rowCkip["NAME_KANA_SEI"]."　".$rowCkip["NAME_KANA_MEI"]);
                if ($lenchk > $lentbl[$key]) {
                    $chkflg = false;
                }
            } else {
                if (strlen($rowCkip[$key]) > $lentbl[$key]) {
                    $chkflg = false;
                }
            }
        }
        return $chkflg;
    }
}
