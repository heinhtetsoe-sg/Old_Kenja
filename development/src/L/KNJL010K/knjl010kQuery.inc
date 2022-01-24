<?php

require_once('for_php7.php');

class knjl010kQuery extends Query {

    //試験区分
    function getExamName($year,$cd)
    {
        return $query = "SELECT namecd2,name1 FROM v_name_mst WHERE year = '".$year."' AND namecd1 = '".$cd."' ";
    }

    //出願区分
    function getExamName2($year,$cd)
    {
        return $query = "SELECT namecd2,name1 FROM v_name_mst WHERE year = '".$year."' AND namecd1 = '".$cd."' AND namecd2 IN ('1','2') ";
    }

    //合格コース
    function getExamCourse($year)
    {
        return "SELECT coursecd,majorcd,examcoursecd,examcourse_name FROM entexam_course_mst WHERE entexamyear = '".$year."' ";
    }

    //実行
    #2005/09/09 arakaki クラブ推薦者、無条件合格対応(無条件合格で取込)
    function InsertQuery(&$model,&$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $cnt = 0;   //処理件数
        //UPDATE or INSERT or DELETE
        $count = get_count($data_arr);    //←事前相談もヘッダが1行に変更 2005/08/10
        for($i=0; $i<$count; $i++)
        {
            switch($model->target){
                case "01"://事前相談データ
                    $table = entexam_consultation_hdat;
                    if ($model->field["WHICH_WAY"] == 1){
                        $query  = " SELECT '' as name FROM ".$table ;    //2006.01.27 alp m-yama
                    }else {
                        $query  = " SELECT name FROM ".$table ;
                    }
                    $query .= "  WHERE entexamyear = '".$model->year."' ";
                    $query .= "    AND create_date = '".str_replace("/", "-", $data_arr[$i]["CREATE_DATE"])."' ";
                    $query .= "    AND testdiv     = '".$data_arr[$i]["TESTDIV"]."' ";
                    $query .= "    AND name        = '".$data_arr[$i]["NAME"]."' ";
                    $query .= "    AND VALUE(name_kana,'')   = '".$data_arr[$i]["NAME_KANA"]."' ";    //2006.01.31 alp m-yama
                    break;
                case "02"://志願者（速報）データ
                    $table = entexam_newsflash_dat;
                    $query  = " SELECT examno FROM ".$table ;
                    $query .= "  WHERE examno      = '".$data_arr[$i]["EXAMNO"]."' ";
                    $query .= "    AND entexamyear = '".$model->year."' ";
                    $query .= "    AND testdiv     = '".$model->testdiv."' ";
                    break;
                case "03"://志願者（基礎）データ
                case "06"://志願者（附属推薦または中高一貫）データ
                case "07"://クラブ推薦者データ
                case "09"://スカラ対象データ
                case "10"://出身塾データ
                    $table = entexam_applicantbase_dat;
                    $query  = " SELECT examno FROM ".$table ;
                    $query .= "  WHERE examno      = '".$data_arr[$i]["EXAMNO"]."' ";
                    $query .= "    AND entexamyear = '".$model->year."' ";
                    //UPDATE 2005/12/29 by OCC
                    if ($model->target == "06"){
                        $query .= "    AND testdiv     = '".$data_arr[$i]["TESTDIV"]."' ";
                    }else{
                        $query .= "    AND testdiv     = '".$model->testdiv."' ";
                    }
                    break;
                case "04"://志願者（住所）データ
                    $table = entexam_applicantaddr_dat;
                    $query  = " SELECT examno FROM ".$table ;
                    $query .= "  WHERE examno      = '".$data_arr[$i]["EXAMNO"]."' ";
                    $query .= "    AND entexamyear = '".$model->year."' ";
                    $query .= "    AND testdiv     = '".$data_arr[$i]["TESTDIV"]."' ";
                    break;
                case "05"://志願者（内申）データ
                    $table = entexam_applicantconfrpt_dat;
                    $query  = " SELECT examno FROM ".$table ;
                    $query .= "  WHERE examno = '".$data_arr[$i]["EXAMNO"]."' ";
                    $query .= "    AND entexamyear = '".$model->year."' ";
                    $query .= "    AND testdiv     = '".$model->testdiv."' ";
                    break;
                case "08"://得点データ
                    $table = entexam_score_dat;
                    $query  = " SELECT examno FROM ".$table ;
                    $query .= "  WHERE examno         = '".$data_arr[$i]["EXAMNO"]."' ";
                    $query .= "    AND testsubclasscd = '".$model->testsubclasscd."' ";
                    $query .= "    AND entexamyear    = '".$model->year."' ";
                    $query .= "    AND testdiv        = '".$model->testdiv."' ";
                    break;
            }
            $examno = $db->getOne($query);

            //更新、一致する受験番号だけ更新
            if($model->field["WHICH_WAY"] == 2 && $examno != "")
            {
                switch($model->target){
                    case "01"://事前相談データ
                        //ACCEPTNO を取得
                        $acceptno  = $db->getOne("SELECT acceptno FROM entexam_consultation_hdat WHERE entexamyear = '".$model->year."' AND create_date = '".str_replace("/", "-", $data_arr[$i]["CREATE_DATE"])."' AND testdiv = '".$data_arr[$i]["TESTDIV"]."' AND name = '".$data_arr[$i]["NAME"]."' AND VALUE(name_kana,'') = '".$data_arr[$i]["NAME_KANA"]."'");//2005.08.09 minei    //2006.01.31 alp m-yama
                        //作成日付
                        if(strlen($data_arr[$i]["CREATE_DATE"])>0){
                            $data["CREATE_DATE"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["CREATE_DATE"])."')";
                        }else{
                            $data["CREATE_DATE"][FUNC] = Null;
                        }
                        //受験番号 2005/10/20 minei
                        $data["EXAMNO"][TEXT] = Null;
                        //氏名かな、性別コード
                        $data["NAME_KANA"][TEXT]    = $data_arr[$i]["NAME_KANA"];
                        $data["SEX"][TEXT]          = $data_arr[$i]["SEX"];
                        //塾情報（登録日付、コード、実力テスト得点、平均、得点-平均、五木偏差値、各種模擬偏差値）
                        if(strlen($data_arr[$i]["PS_UPDATED"])>0){
                            $data["PS_UPDATED"][FUNC]   = "date('".str_replace("/", "-", $data_arr[$i]["PS_UPDATED"])."')";
                            $data["PS_ACCEPTNO"][TEXT]  = sprintf("%04d",$acceptno);
                        }else{
                            $data["PS_UPDATED"][FUNC]   = Null;
                            $data["PS_ACCEPTNO"][TEXT]  = Null;
                        }
                        $data["PS_CD"][TEXT]        = $data_arr[$i]["PS_CD"];
                        $data["PS_ITEM1"][NUMBER]   = $data_arr[$i]["PS_ITEM1"];
                        $data["PS_ITEM2"][NUMBER]   = $data_arr[$i]["PS_ITEM2"];
                        $data["PS_ITEM3"][NUMBER]   = $data_arr[$i]["PS_ITEM3"];
                        $data["PS_ITEM4"][NUMBER]   = $data_arr[$i]["PS_ITEM4"];
                        $data["PS_ITEM5"][NUMBER]   = $data_arr[$i]["PS_ITEM5"];
                        //出身学校情報（登録日付、コード、実力テスト得点、平均、得点-平均、五木偏差値、各種模擬偏差値）
                        if(strlen($data_arr[$i]["FS_UPDATED"])>0){
                            $data["FS_UPDATED"][FUNC]   = "date('".str_replace("/", "-", $data_arr[$i]["FS_UPDATED"])."')";
                            $data["FS_ACCEPTNO"][TEXT]  = sprintf("%04d",$acceptno);
                        }else{
                            $data["FS_UPDATED"][FUNC]   = Null;
                            $data["FS_ACCEPTNO"][TEXT]  = Null;
                        }
                        $data["FS_CD"][TEXT]        = $data_arr[$i]["FS_CD"];
                        $data["FS_ITEM1"][NUMBER]   = $data_arr[$i]["FS_ITEM1"];
                        $data["FS_ITEM2"][NUMBER]   = $data_arr[$i]["FS_ITEM2"];
                        $data["FS_ITEM3"][NUMBER]   = $data_arr[$i]["FS_ITEM3"];
                        $data["FS_ITEM4"][NUMBER]   = $data_arr[$i]["FS_ITEM4"];
                        $data["FS_ITEM5"][NUMBER]   = $data_arr[$i]["FS_ITEM5"];
                        //登録者コード、更新日付
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "SYSDATE()";

                        $where  = " WHERE create_date = '".str_replace("/", "-", $data_arr[$i]["CREATE_DATE"])."' ";
                        $where .= "   AND name        = '".$data_arr[$i]["NAME"]."' ";
                        $where .= "   AND entexamyear = '".$model->year."' ";
                        $where .= "   AND testdiv     = '".$data_arr[$i]["TESTDIV"]."' ";
                        break;

                    case "02"://志願者（速報）データ
                        //UPDATE 2005/12/30 by OCC
                        $shdiv = knjl010kQuery::getOne($db,"SELECT namecd2 FROM name_mst WHERE NAMECD1='L006' AND abbv1='".$data_arr[$i]["SHDIV"]."'");

                        $data["TESTDIV"][TEXT]      = $model->testdiv;
                        if(strlen($data_arr[$i]["RECEPT_DATE"])>0){
                            $data["RECEPT_DATE"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["RECEPT_DATE"])."')";
                        }else{
                            $data["RECEPT_DATE"][FUNC] = Null;
                        }
                        $data["SHDIV"][TEXT]        = $shdiv;
                        $data["DESIREDIV"][TEXT]    = $data_arr[$i]["DESIREDIV"];
                        $data["SEX"][TEXT]          = $data_arr[$i]["SEX"];
                        $data["NATPUBPRIDIV"][TEXT] = $data_arr[$i]["NATPUBPRIDIV"];
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "SYSDATE()";

                        $where  = " WHERE examno      = '".$data_arr[$i]["EXAMNO"]."'";
                        $where .= "   AND entexamyear = '".$model->year."' ";
                        $where .= "   AND testdiv = '".$model->testdiv."' ";
                        break;
                    case "03"://志願者（基礎）データ
                    case "06"://志願者（附属推薦または中高一貫）データ
                        //UPDATE 2005/12/30 by OCC
                        $shdiv = knjl010kQuery::getOne($db,"SELECT namecd2 FROM name_mst WHERE NAMECD1='L006' AND abbv1='".$data_arr[$i]["SHDIV"]."'");

                        if(strlen($data_arr[$i]["RECEPT_DATE"])>0){
                            $data["RECEPT_DATE"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["RECEPT_DATE"])."')";
                        }else{
                            $data["RECEPT_DATE"][FUNC] = Null;
                        }
                        $data["SHDIV"][TEXT]                  = $shdiv;
                        $data["DESIREDIV"][TEXT]              = $data_arr[$i]["DESIREDIV"];
                        $data["SPECIAL_REASON_DIV"][TEXT]     = $data_arr[$i]["SPECIAL_REASON_DIV"];
                        $data["SUCCESS_NOTICENO"][TEXT]   = Null;                       //合格通知NO 2005/11/08 arakaki
                        if($model->target == "03"){
                            $data["APPLICANTDIV"][TEXT]       = $data_arr[$i]["APPLICANTDIV"];
                        }elseif($model->target == "06"){
                            $data["APPLICANTDIV"][TEXT]       = $data_arr[$i]["APPLICANTDIV"];
                            $data["JUDGEMENT"][TEXT]          = "4";
                            $data["REGULARSUCCESS_FLG"][TEXT] = 0;
                            $data["SUC_COURSECD"][TEXT]       = $data_arr[$i]["SUC_COURSECD"];
                            $data["SUC_MAJORCD"][TEXT]        = $data_arr[$i]["SUC_MAJORCD"];
                            $data["SUC_COURSECODE"][TEXT]     = $data_arr[$i]["SUC_COURSECODE"];
                            $data["PROCEDUREDIV"][TEXT]       = 2;  // 手続済み
                            $data["ENTDIV"][TEXT]             = 2;  // 入学あり  2005.08.29 minei
                        }
                        $data["NAME"][TEXT]         = rtrim($data_arr[$i]["NAME"]);
                        $data["NAME_KANA"][TEXT]    = rtrim($data_arr[$i]["NAME_KANA"]);
                        $data["SEX"][TEXT]          = $data_arr[$i]["SEX"];
                        if(strlen($data_arr[$i]["BIRTHDAY"])>0){
                            $data["BIRTHDAY"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["BIRTHDAY"])."')";
                        }else{
                            $data["BIRTHDAY"][FUNC] = Null;
                        }
                        $data["ADDRESSCD"][TEXT]    = $data_arr[$i]["ADDRESSCD"];
                        $data["TELNO"][TEXT]        = $data_arr[$i]["TELNO"];
                        $data["LOCATIONCD"][TEXT]   = $data_arr[$i]["LOCATIONCD"];
                        $data["NATPUBPRIDIV"][TEXT] = $data_arr[$i]["NATPUBPRIDIV"];
                        $data["FS_CD"][TEXT]        = $data_arr[$i]["FS_CD"];
                        $data["FS_GRDYEAR"][TEXT]   = $data_arr[$i]["FS_GRDYEAR"];
                        $data["FS_HRCLASS"][TEXT]   = $data_arr[$i]["FS_HRCLASS"];
                        $data["FS_ATTENDNO"][TEXT]  = $data_arr[$i]["FS_ATTENDNO"];
                        $data["PS_CD"][TEXT]        = $data_arr[$i]["PS_CD"];
                        $data["APPROVAL_FLG"][TEXT] = $data_arr[$i]["APPROVAL_FLG"];    # 2005/11/08 arakaki
                        $data["GNAME"][TEXT]        = $data_arr[$i]["GNAME"];
                        $data["GKANA"][TEXT]        = $data_arr[$i]["GKANA"];
                        $data["GTELNO"][TEXT]       = $data_arr[$i]["GTELNO"];
                        $data["FORMNO"][TEXT]       = $data_arr[$i]["FORMNO"];
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "SYSDATE()";

                        $where  = " WHERE examno      = '".$data_arr[$i]["EXAMNO"]."'";
                        $where .= "   AND entexamyear = '".$model->year."' ";
                        //2005/12/29 by OCC
                        if($model->target == "03"){
                            $where .= "   AND testdiv     = '".$model->testdiv."' ";
                        }else{
                            $where .= "   AND testdiv     = '".$data_arr[$i]["TESTDIV"]."' ";
                        }
                        break;
                    case "04"://志願者（住所）データ
                        $data["ZIPCD"][TEXT]         = $data_arr[$i]["ZIPCD"];
                        $data["ADDRESS"][TEXT]       = $data_arr[$i]["ADDRESS"];
                        $data["GZIPCD"][TEXT]        = $data_arr[$i]["GZIPCD"];
                        $data["GADDRESS"][TEXT]      = $data_arr[$i]["GADDRESS"];
                        $data["REGISTERCD"][TEXT]    = STAFFCD;
                        $data["UPDATED"][FUNC]       = "SYSDATE()";

                        $where  = " WHERE examno       = '".$data_arr[$i]["EXAMNO"]."'";
                        $where .= "    AND entexamyear = '".$model->year."' ";
                        $where .= "   AND testdiv     = '".$data_arr[$i]["TESTDIV"]."' ";
                        break;
                    case "05"://志願者（内申）データ
                        $data["TESTDIV"][TEXT]              = $model->testdiv;
                        $data["CONFIDENTIAL_RPT01"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT1"];
                        $data["CONFIDENTIAL_RPT02"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT2"];
                        $data["CONFIDENTIAL_RPT03"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT3"];
                        $data["CONFIDENTIAL_RPT04"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT4"];
                        $data["CONFIDENTIAL_RPT05"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT5"];
                        $data["CONFIDENTIAL_RPT06"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT6"];
                        $data["CONFIDENTIAL_RPT07"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT7"];
                        $data["CONFIDENTIAL_RPT08"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT8"];
                        $data["CONFIDENTIAL_RPT09"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT9"];
                        $data["CONFIDENTIAL_RPT10"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT10"];
                        $data["CONFIDENTIAL_RPT11"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT11"];
                        $data["CONFIDENTIAL_RPT12"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT12"];
                        $data["TOTAL_REPORT"][NUMBER]       = $data_arr[$i]["TOTAL_REPORT"];
                        $data["AVERAGE5"][NUMBER]           = $data_arr[$i]["AVERAGE5"];
                        $data["AVERAGE_ALL"][NUMBER]        = $data_arr[$i]["AVARAGE_ALL"];
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][FUNC]              = "SYSDATE()";

                        $where  = " WHERE examno      = '".$data_arr[$i]["EXAMNO"]."'";
                        $where .= "   AND entexamyear = '".$model->year."' ";
                        $where .= "   AND testdiv     = '".$model->testdiv."' ";
                        break;
                    case "07"://クラブ推薦者データ
                        //志望課程コード, 学科コード,コースコード取得(MIN志望連番)   #2005/09/12 arakaki
                        $query  = " SELECT T01.ENTEXAMYEAR,T01.TESTDIV,T01.DESIREDIV,T01.WISHNO,T01.COURSECD,T01.MAJORCD,T01.EXAMCOURSECD ";
                        $query .= "   FROM ENTEXAM_WISHDIV_MST AS T01, ";
                        $query .= "        (SELECT ENTEXAMYEAR,TESTDIV,DESIREDIV,MIN(WISHNO) AS MIN_WISHNO ";
                        $query .= "           FROM ENTEXAM_WISHDIV_MST ";
                        $query .= "          WHERE ENTEXAMYEAR= '".$model->year."' ";
                        $query .= "            AND TESTDIV= '".$model->testdiv."' ";
                        $query .= "            AND DESIREDIV =(SELECT DESIREDIV ";
                        $query .= "                              FROM ENTEXAM_APPLICANTBASE_DAT ";
                        $query .= "                             WHERE ENTEXAMYEAR= '".$model->year."' ";
                        $query .= "                               AND TESTDIV= '".$model->testdiv."' ";
                        $query .= "                               AND EXAMNO= '".$data_arr[$i]["EXAMNO"]."') ";
                        $query .= "         GROUP BY ENTEXAMYEAR,TESTDIV,DESIREDIV) AS T02 ";
                        $query .= "  WHERE T01.ENTEXAMYEAR=T02.ENTEXAMYEAR ";
                        $query .= "    AND T01.TESTDIV=T02.TESTDIV ";
                        $query .= "    AND T01.DESIREDIV=T02.DESIREDIV ";
                        $query .= "    AND T01.WISHNO=T02.MIN_WISHNO ";

                        $wishdiv_dt = $db->getRow($query, DB_FETCHMODE_ASSOC);

                        $data["APPLICANTDIV"][TEXT]       =  3;                          //出願区分←3:クラブ推薦      2005/09/09 arakaki
                        $data["JUDGEMENT"][TEXT]          = "4";                         //合否判定←4:推薦合格        2005/09/09 arakaki
                        $data["REGULARSUCCESS_FLG"][TEXT] =  0;                          //正規合格フラグ←0:正規            2005/09/09 arakaki
                        $data["SUC_COURSECD"][TEXT]       = $wishdiv_dt["COURSECD"];     //合格課程コード                    2005/09/09 arakaki
                        $data["SUC_MAJORCD"][TEXT]        = $wishdiv_dt["MAJORCD"];      //合格学科コード                    2005/09/09 arakaki
                        $data["SUC_COURSECODE"][TEXT]     = $wishdiv_dt["EXAMCOURSECD"]; //合格コースコード                  2005/09/09 arakaki
                        $data["PROCEDUREDIV"][TEXT]       = 2;                           //手続区分←2:済                    2005/09/09 arakaki
                        $data["ENTDIV"][TEXT]             = 2;                           //入学区分←2:有り                  2005/09/09 arakaki
                        $data["SUCCESS_NOTICENO"][TEXT]    = Null;                       //合格通知NO 2005/11/08 arakaki

                        $data["CLUBCD"][TEXT]    = $data_arr[$i]["CLUBCD"];                 //クラブコード 2006.01.17 alp m-yama

                        $where  = " WHERE examno      = '".$data_arr[$i]["EXAMNO"]."'";
                        $where .= "   AND entexamyear = '".$model->year."' ";
                        $where .= "   AND testdiv     = '".$model->testdiv."' ";
                        break;
                    case "08"://得点データ
                        $data["TESTDIV"][TEXT]        = $model->testdiv;
                        $data["TESTSUBCLASSCD"][TEXT] = $model->testsubclasscd;
                        $data["ATTEND_FLG"][TEXT]     = (strlen($data_arr[$i]["A_SCORE"])>0 ? '1' : '0');
                        $data["A_SCORE"][NUMBER]      = $data_arr[$i]["A_SCORE"];
                        $data["A_STD_SCORE"][NUMBER]  = 0;
                        $data["A_RANK"][NUMBER]       = 0;
                        $data["B_SCORE"][NUMBER]      = $data_arr[$i]["B_SCORE"];
                        $data["B_STD_SCORE"][NUMBER]  = 0;
                        $data["B_RANK"][NUMBER]       = 0;
                        $data["REGISTERCD"][TEXT]     = STAFFCD;
                        $data["UPDATED"][FUNC]        = "SYSDATE()";

                        $where  = " WHERE examno         = '".$data_arr[$i]["EXAMNO"]."'";
                        $where .= "   AND testsubclasscd = '".$model->testsubclasscd."' ";
                        $where .= "   AND entexamyear    = '".$model->year."' ";
                        $where .= "   AND testdiv        = '".$model->testdiv."' ";
                        break;
                    case "09"://スカラ対象データ
                        $data["SCALASHIPDIV"][TEXT] = $data_arr[$i]["SCALASHIPDIV"];
                        $where  = " WHERE examno      = '".$data_arr[$i]["EXAMNO"]."'";
                        $where .= "   AND entexamyear = '".$model->year."' ";
                        $where .= "   AND testdiv     = '".$model->testdiv."' ";
                        break;
                    case "10"://出身塾データ
                        $data["PS_CD"][TEXT] = $data_arr[$i]["PS_CD"];
                        $where  = " WHERE examno      = '".$data_arr[$i]["EXAMNO"]."'";
                        $where .= "   AND entexamyear = '".$model->year."' ";
                        $where .= "   AND testdiv     = '".$model->testdiv."' ";
                        break;
                }
                $query = Query::updateSQL($data, $table, $where);
                $db->query($query);

                if($model->target == "01") knjl010kQuery::InsertConsult($db,$model,$data_arr[$i],2,$i);

                $cnt++;

            //追加、一致しない受験番号だけ追加(7:クラブ推薦者, 9:スカラ対象者, 10:出身塾 は処理なし)
            }elseif($model->field["WHICH_WAY"] == 1 && $examno == "")
            {
                switch($model->target){
                    case "01"://事前相談データ
                        //ACCEPTNO 連番の最大値を取得
                        $maxno = $db->getOne("SELECT MAX(INTEGER(CASE WHEN acceptno IS NULL THEN '0'
                                                                      WHEN acceptno = '' THEN '0'
                                                                      ELSE acceptno END))
                                                FROM entexam_consultation_hdat
                                               WHERE entexamyear = '".$model->year."'");
                        $maxno = $maxno + 1;



                        // ACCEPTNOが9999を超えたら処理中止 2005/08/10 追加
                        if ($maxno > 9999){
                            Query::dbCheckIn($db);
                            $this->setWarning("MSG915","受付番号が9999を超えます。");
                            return;
                        }
                        //年度、試験区分、受付番号
                        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                        $data["TESTDIV"][TEXT]      = $data_arr[$i]["TESTDIV"];
                        $data["ACCEPTNO"][TEXT]     = sprintf("%04d",$maxno);     //<---取得した最大値＋１
                        if(strlen($data_arr[$i]["CREATE_DATE"])>0){
                            $data["CREATE_DATE"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["CREATE_DATE"])."')";
                        }else{
                            $data["CREATE_DATE"][FUNC] = Null;
                        }
                        //受験番号 2005/10/20 minei
                        $data["EXAMNO"][TEXT] = Null;
                        //氏名、氏名かな、性別コード
                        $data["NAME"][TEXT]         = rtrim($data_arr[$i]["NAME"]);
                        $data["NAME_KANA"][TEXT]    = rtrim($data_arr[$i]["NAME_KANA"]);
                        $data["SEX"][TEXT]          = $data_arr[$i]["SEX"];
                        //塾情報（登録日付、コード、実力テスト得点、平均、得点-平均、五木偏差値、各種模擬偏差値）
                        if(strlen($data_arr[$i]["PS_UPDATED"])>0){
                            $data["PS_UPDATED"][FUNC]   = "date('".str_replace("/", "-", $data_arr[$i]["PS_UPDATED"])."')";
                            $data["PS_ACCEPTNO"][TEXT]  = sprintf("%04d",$maxno);
                        }else{
                            $data["PS_UPDATED"][FUNC]   = Null;
                            $data["PS_ACCEPTNO"][TEXT]  = Null;
                        }
                        $data["PS_CD"][TEXT]        = $data_arr[$i]["PS_CD"];
                        $data["PS_ITEM1"][NUMBER]   = $data_arr[$i]["PS_ITEM1"];
                        $data["PS_ITEM2"][NUMBER]   = $data_arr[$i]["PS_ITEM2"];
                        $data["PS_ITEM3"][NUMBER]   = $data_arr[$i]["PS_ITEM3"];
                        $data["PS_ITEM4"][NUMBER]   = $data_arr[$i]["PS_ITEM4"];
                        $data["PS_ITEM5"][NUMBER]   = $data_arr[$i]["PS_ITEM5"];
                        //出身学校情報（登録日付、コード、実力テスト得点、平均、得点-平均、五木偏差値、各種模擬偏差値）
                        if(strlen($data_arr[$i]["FS_UPDATED"])>0){
                            $data["FS_UPDATED"][FUNC]   = "date('".str_replace("/", "-", $data_arr[$i]["FS_UPDATED"])."')";
                            $data["FS_ACCEPTNO"][TEXT]  = sprintf("%04d",$maxno);
                        }else{
                            $data["FS_UPDATED"][FUNC]   = Null;
                            $data["FS_ACCEPTNO"][TEXT]  = Null;
                        }
                        $data["FS_CD"][TEXT]        = $data_arr[$i]["FS_CD"];
                        $data["FS_ITEM1"][NUMBER]   = $data_arr[$i]["FS_ITEM1"];
                        $data["FS_ITEM2"][NUMBER]   = $data_arr[$i]["FS_ITEM2"];
                        $data["FS_ITEM3"][NUMBER]   = $data_arr[$i]["FS_ITEM3"];
                        $data["FS_ITEM4"][NUMBER]   = $data_arr[$i]["FS_ITEM4"];
                        $data["FS_ITEM5"][NUMBER]   = $data_arr[$i]["FS_ITEM5"];
                        //登録者コード、更新日付
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "SYSDATE()";
                        break;

                    case "02"://志願者（速報）データ
                        //UPDATE 2005/12/30 by OCC
                        $shdiv = knjl010kQuery::getOne($db,"SELECT namecd2 FROM name_mst WHERE NAMECD1='L006' AND abbv1='".$data_arr[$i]["SHDIV"]."'");

                        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                        $data["TESTDIV"][TEXT]      = $model->testdiv;
                        $data["EXAMNO"][TEXT]       = $data_arr[$i]["EXAMNO"];
                        if(strlen($data_arr[$i]["RECEPT_DATE"])>0){
                            $data["RECEPT_DATE"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["RECEPT_DATE"])."')";
                        }else{
                            $data["RECEPT_DATE"][FUNC] = Null;
                        }
                        $data["SHDIV"][TEXT]        = $shdiv;
                        $data["DESIREDIV"][TEXT]    = $data_arr[$i]["DESIREDIV"];
                        $data["SEX"][TEXT]          = $data_arr[$i]["SEX"];
                        $data["NATPUBPRIDIV"][TEXT] = $data_arr[$i]["NATPUBPRIDIV"];
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "SYSDATE()";
                        break;
                    case "03"://志願者（基礎）データ
                    case "06"://志願者（附属推薦または中高一貫）データ
                        //UPDATE 2005/12/30 by OCC
                        $shdiv = knjl010kQuery::getOne($db,"SELECT namecd2 FROM name_mst WHERE NAMECD1='L006' AND abbv1='".$data_arr[$i]["SHDIV"]."'");

                        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
                        //2005/12/29 by OCC
                        if($model->target == "03"){
                            $data["TESTDIV"][TEXT]      = $model->testdiv;
                        }else{
                            $data["TESTDIV"][TEXT]      = $data_arr[$i]["TESTDIV"];
                        }
                        $data["EXAMNO"][TEXT]       = $data_arr[$i]["EXAMNO"];
                        if(strlen($data_arr[$i]["RECEPT_DATE"])>0){
                            $data["RECEPT_DATE"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["RECEPT_DATE"])."')";
                        }else{
                            $data["RECEPT_DATE"][FUNC] = Null;
                        }
                        $data["SHDIV"][TEXT]                  = $shdiv;
                        $data["DESIREDIV"][TEXT]              = $data_arr[$i]["DESIREDIV"];
                        $data["SPECIAL_REASON_DIV"][TEXT]     = $data_arr[$i]["SPECIAL_REASON_DIV"];
                        if($model->target == "03"){
                            $data["APPLICANTDIV"][TEXT]       = 0;
                        }elseif($model->target == "06"){
                            $data["APPLICANTDIV"][TEXT]       = $data_arr[$i]["APPLICANTDIV"];
                            $data["JUDGEMENT"][TEXT]          = "4";
                            $data["REGULARSUCCESS_FLG"][TEXT] = 0;
                            //2005/12/29 by OCC
                            $data["SUC_COURSECD"][TEXT]       = $data_arr[$i]["SUC_COURSECD"];
                            $data["SUC_MAJORCD"][TEXT]        = $data_arr[$i]["SUC_MAJORCD"];
                            $data["SUC_COURSECODE"][TEXT]     = $data_arr[$i]["SUC_COURSECODE"];
                            
                            $data["PROCEDUREDIV"][TEXT]       = 2;  // 手続済み
                            $data["ENTDIV"][TEXT]             = 2;  // 入学あり  2005.08.29 minei
                        }
                        $data["NAME"][TEXT]         = rtrim($data_arr[$i]["NAME"]);
                        $data["NAME_KANA"][TEXT]    = rtrim($data_arr[$i]["NAME_KANA"]);
                        $data["SEX"][TEXT]          = $data_arr[$i]["SEX"];
                        if(strlen($data_arr[$i]["BIRTHDAY"])>0)
                        {
                            $data["BIRTHDAY"][FUNC] = "date('".str_replace("/", "-", $data_arr[$i]["BIRTHDAY"])."')";
                        }else{
                            $data["BIRTHDAY"][FUNC] = Null;
                        }
                        $data["ADDRESSCD"][TEXT]    = $data_arr[$i]["ADDRESSCD"];
                        $data["TELNO"][TEXT]        = $data_arr[$i]["TELNO"];
                        $data["LOCATIONCD"][TEXT]   = $data_arr[$i]["LOCATIONCD"];
                        $data["NATPUBPRIDIV"][TEXT] = $data_arr[$i]["NATPUBPRIDIV"];
                        $data["FS_CD"][TEXT]        = $data_arr[$i]["FS_CD"];
                        $data["FS_GRDYEAR"][TEXT]   = $data_arr[$i]["FS_GRDYEAR"];
                        $data["FS_HRCLASS"][TEXT]   = $data_arr[$i]["FS_HRCLASS"];
                        $data["FS_ATTENDNO"][TEXT]  = $data_arr[$i]["FS_ATTENDNO"];
                        $data["PS_CD"][TEXT]        = $data_arr[$i]["PS_CD"];
                        $data["APPROVAL_FLG"][TEXT] = $data_arr[$i]["APPROVAL_FLG"];    # 2005/11/08 arakaki
                        $data["GNAME"][TEXT]        = $data_arr[$i]["GNAME"];
                        $data["GKANA"][TEXT]        = $data_arr[$i]["GKANA"];
                        $data["GTELNO"][TEXT]       = $data_arr[$i]["GTELNO"];
                        $data["FORMNO"][TEXT]       = $data_arr[$i]["FORMNO"];
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][FUNC]      = "SYSDATE()";
                        break;
                    case "04"://志願者（住所）データ
                        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
                        $data["TESTDIV"][TEXT]        = $data_arr[$i]["TESTDIV"];
                        $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
                        $data["ZIPCD"][TEXT]         = $data_arr[$i]["ZIPCD"];
                        $data["ADDRESS"][TEXT]       = $data_arr[$i]["ADDRESS"];
                        $data["GZIPCD"][TEXT]        = $data_arr[$i]["GZIPCD"];
                        $data["GADDRESS"][TEXT]      = $data_arr[$i]["GADDRESS"];
                        $data["REGISTERCD"][TEXT]    = STAFFCD;
                        $data["UPDATED"][FUNC]       = "SYSDATE()";
                        break;
                    case "05"://志願者（内申）データ
                        $data["ENTEXAMYEAR"][TEXT]          = $model->year;
                        $data["TESTDIV"][TEXT]              = $model->testdiv;
                        $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
                        $data["CONFIDENTIAL_RPT01"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT1"];
                        $data["CONFIDENTIAL_RPT02"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT2"];
                        $data["CONFIDENTIAL_RPT03"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT3"];
                        $data["CONFIDENTIAL_RPT04"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT4"];
                        $data["CONFIDENTIAL_RPT05"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT5"];
                        $data["CONFIDENTIAL_RPT06"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT6"];
                        $data["CONFIDENTIAL_RPT07"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT7"];
                        $data["CONFIDENTIAL_RPT08"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT8"];
                        $data["CONFIDENTIAL_RPT09"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT9"];
                        $data["CONFIDENTIAL_RPT10"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT10"];
                        $data["CONFIDENTIAL_RPT11"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT11"];
                        $data["CONFIDENTIAL_RPT12"][NUMBER] = $data_arr[$i]["CONFIDENTIAL_RPT12"];
                        $data["TOTAL_REPORT"][NUMBER]       = $data_arr[$i]["TOTAL_REPORT"];
                        $data["AVERAGE5"][NUMBER]           = $data_arr[$i]["AVERAGE5"];
                        $data["AVERAGE_ALL"][NUMBER]        = $data_arr[$i]["AVARAGE_ALL"];
                        $data["REGISTERCD"][TEXT]           = STAFFCD;
                        $data["UPDATED"][FUNC]              = "SYSDATE()";
                        break;
                    case "08"://得点データ
                        $data["ENTEXAMYEAR"][TEXT]    = $model->year;
                        $data["TESTDIV"][TEXT]        = $model->testdiv;
                        $data["EXAMNO"][TEXT]         = $data_arr[$i]["EXAMNO"];
                        $data["TESTSUBCLASSCD"][TEXT] = $model->testsubclasscd;
                        $data["ATTEND_FLG"][TEXT]     = (strlen($data_arr[$i]["A_SCORE"])>0 ? '1' : '0');
                        $data["A_SCORE"][NUMBER]      = $data_arr[$i]["A_SCORE"];
                        $data["A_STD_SCORE"][NUMBER]  = 0;
                        $data["A_RANK"][NUMBER]       = 0;
                        $data["B_SCORE"][NUMBER]      = $data_arr[$i]["B_SCORE"];
                        $data["B_STD_SCORE"][NUMBER]  = 0;
                        $data["B_RANK"][NUMBER]       = 0;
                        $data["REGISTERCD"][TEXT]     = STAFFCD;
                        $data["UPDATED"][FUNC]        = "SYSDATE()";
                        break;
                }
                $query = Query::insertSQL($data, $table);
                $db->query($query);

                if($model->target == "01") knjl010kQuery::InsertConsult($db,$model,$data_arr[$i],1,$i);    #2005/08/09 arakaki

                $cnt++;

            //削除(または取消)、一致する受験番号だけ削除(またはnullで更新)
            }elseif($model->field["WHICH_WAY"] == 3 && $examno!="")
            {
                switch($model->target){
                    case "01"://事前相談データ
                        knjl010kQuery::InsertConsult($db,$model,$data_arr[$i],3,$i);    #2005/08/09 arakaki
                        $query  = " DELETE FROM entexam_consultation_hdat ";
                        $query .= "       WHERE entexamyear = '".$model->year."' ";
                        $query .= "         AND create_date = '".str_replace("/", "-", $data_arr[$i]["CREATE_DATE"])."' ";
                        $query .= "         AND testdiv     = '".$data_arr[$i]["TESTDIV"]."' ";
                        $query .= "         AND name        = '".$data_arr[$i]["NAME"]."' ";
                        $query .= "         AND VALUE(name_kana,'')   = '".$data_arr[$i]["NAME_KANA"]."' ";//2005.08.09 minei    //2006.01.31 alp m-yama
                        break;
                    case "02"://志願者（速報）データ
                    case "03"://志願者（基礎）データ
                    case "04"://志願者（住所）データ
                    case "05"://志願者（内申）データ
                    case "06"://志願者（附属推薦または中高一貫）データ
                        $query  = " DELETE FROM ".$table ;
                        $query .= "       WHERE examno      = '".$data_arr[$i]["EXAMNO"]."' ";
                        $query .= "         AND entexamyear = '".$model->year."' ";
                        //2005/12/29 by OCC
                        if ($model->target == "04" || $model->target == "06"){
                            $query .= "         AND testdiv     = '".$data_arr[$i]["TESTDIV"]."' ";
                        }else{
                            $query .= "         AND testdiv     = '".$model->testdiv."' ";
                        }
                        break;
                    case "07"://クラブ推薦者データ
                    case "09"://スカラシップ
                    case "10"://出身塾
                        if($model->target == "07"){
                            $data["APPLICANTDIV"][TEXT]       = 0;      //出願区分←0:一般
                            $data["JUDGEMENT"][TEXT]          = Null;   //合否判定←4:推薦合格    2005/09/09 arakaki
                            $data["REGULARSUCCESS_FLG"][TEXT] = Null;   //正規合格フラグ←null    2005/09/09 arakaki
                            $data["SUC_COURSECD"][TEXT]       = Null;   //合格課程コード          2005/09/09 arakaki
                            $data["SUC_MAJORCD"][TEXT]        = Null;   //合格学科コード          2005/09/09 arakaki
                            $data["SUC_COURSECODE"][TEXT]     = Null;   //合格コースコード        2005/09/09 arakaki
                            $data["PROCEDUREDIV"][TEXT]       = 1;      // 手続区分←1:未         2005/09/09 arakaki
                            $data["ENTDIV"][TEXT]             = Null;   // 入学区分←Null         2005/09/09 arakaki
                            $data["SUCCESS_NOTICENO"][TEXT]   = Null;   //合格通知NO←Null        2005/10/19 minei
                            $data["CLUBCD"][TEXT]              = Null;   //クラブコード            2006/01/17 alp m-yama
                        }elseif($model->target == "09"){
                            $data["SCALASHIPDIV"][TEXT] = Null;
                        }elseif($model->target == "10"){
                            $data["PS_CD"][TEXT] = Null;
                        }
                        $where  = " WHERE examno      = '".$data_arr[$i]["EXAMNO"]."'";
                        $where .= "   AND entexamyear = '".$model->year."' ";
                        $where .= "   AND testdiv     = '".$model->testdiv."' ";
                        $query = Query::updateSQL($data, $table, $where);
                        break;
                    case "08"://得点データ
                        $query  = " DELETE FROM ".$table ;
                        $query .= "       WHERE examno         = '".$data_arr[$i]["EXAMNO"]."' ";
                        $query .= "         AND testsubclasscd = '".$model->testsubclasscd."' ";
                        $query .= "         AND entexamyear    = '".$model->year."' ";
                        $query .= "         AND testdiv        = '".$model->testdiv."' ";
                        break;
                }
                $db->query($query);

                $cnt++;
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //事前相談データの内容部分の実行
    function InsertConsult(&$db,&$model,&$data_arr,$way,$i)
    {
        $table = entexam_consultation_dat;

        //2006/01/14 alp m-yama↓
        if ($way == 3){
            $result = $db->query("SELECT DISTINCT ACCEPTNO FROM entexam_consultation_hdat WHERE entexamyear = '".$model->year."' AND create_date = '".str_replace("/", "-", $data_arr["CREATE_DATE"])."'  AND testdiv = '".$data_arr["TESTDIV"]."' AND value(name,'') = value('".$data_arr["NAME"]."','') AND value(name_kana,'') = value('".$data_arr["NAME_KANA"]."','') "); //2006/01/14 alp m-yama
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $query  = " DELETE FROM ".$table ;
                $query .= "       WHERE entexamyear = '".$model->year."' ";
                $query .= "         AND testdiv     = '".$data_arr["TESTDIV"]."' ";
                $query .= "         AND acceptno    = '".sprintf("%04d",$row["ACCEPTNO"])."' ";
                $db->query($query);
            }
            $result->free();
        }else {
            $acceptno  = $db->getOne("SELECT max(acceptno) FROM entexam_consultation_hdat WHERE entexamyear = '".$model->year."' AND create_date = '".str_replace("/", "-", $data_arr["CREATE_DATE"])."'  AND testdiv = '".$data_arr["TESTDIV"]."' AND value(name,'') = value('".$data_arr["NAME"]."','') AND value(name_kana,'') = value('".$data_arr["NAME_KANA"]."','') "); //2006/01/14 alp m-yama
        }
        //2006/01/14 alp m-yama↑

        if($way == 2){
            $query  = " DELETE FROM ".$table ;
            $query .= "       WHERE entexamyear = '".$model->year."' ";
            $query .= "         AND testdiv     = '".$data_arr["TESTDIV"]."' ";
            $query .= "         AND acceptno    = '".sprintf("%04d",$acceptno)."' ";
            $db->query($query);
        }

        if($way == 1 || $way == 2){
            //出身学校用
            for($divi=1; $divi<5; $divi++)
            {
                if($data_arr["FORG_SHDIV".$divi]){
                    $data["ENTEXAMYEAR"][TEXT]   = $model->year;
                    $data["TESTDIV"][TEXT]       = $data_arr["TESTDIV"];
                    $data["ACCEPTNO"][TEXT]      = sprintf("%04d",$acceptno);
                    $data["DATADIV"][TEXT]       = 1;
                    $data["WISHNO"][TEXT]        = $divi;
                    $data["ORG_SHDIV"][TEXT]     = $data_arr["FORG_SHDIV".$divi];
                    $data["ORG_MAJORCD"][TEXT]   = $data_arr["FORG_MAJOR".$divi];
                    $data["ORG_JUDGEMENT"][TEXT] = $data_arr["FORG_JUDGEMENT".$divi];
                    $data["SHDIV"][TEXT]         = $model->fshdivarr[$i][$divi];            #2005/08/09 arakaki
                    $data["COURSECD"][TEXT]      = substr($model->forgarr[$i][$divi],0,1);  #2005/08/09 arakaki
                    $data["MAJORCD"][TEXT]       = substr($model->forgarr[$i][$divi],1,3);  #2005/08/09 arakaki
                    $data["EXAMCOURSECD"][TEXT]  = substr($model->forgarr[$i][$divi],4,4);  #2005/08/09 arakaki
                    $data["JUDGEMENT"][TEXT]     = $model->fjudgearr[$i][$divi];            #2005/08/09 arakaki
                    $data["REGISTERCD"][TEXT]    = STAFFCD;
                    $data["UPDATED"][FUNC]       = "SYSDATE()";
                    $query = Query::insertSQL($data, $table);
                    $db->query($query);
                }
            }

            //塾用
            for($divi=1; $divi<5; $divi++)
            {
                if($data_arr["PORG_SHDIV".$divi]){
                    $data["ENTEXAMYEAR"][TEXT]   = $model->year;
                    $data["TESTDIV"][TEXT]       = $data_arr["TESTDIV"];
                    $data["ACCEPTNO"][TEXT]      = sprintf("%04d",$acceptno);
                    $data["DATADIV"][TEXT]       = 2;
                    $data["WISHNO"][TEXT]        = $divi;
                    $data["ORG_SHDIV"][TEXT]     = $data_arr["PORG_SHDIV".$divi];
                    $data["ORG_MAJORCD"][TEXT]   = $data_arr["PORG_MAJOR".$divi];
                    $data["ORG_JUDGEMENT"][TEXT] = $data_arr["PORG_JUDGEMENT".$divi];
                    $data["SHDIV"][TEXT]         = $model->pshdivarr[$i][$divi];            #2005/08/09 arakaki
                    $data["COURSECD"][TEXT]      = substr($model->porgarr[$i][$divi],0,1);  #2005/08/09 arakaki
                    $data["MAJORCD"][TEXT]       = substr($model->porgarr[$i][$divi],1,3);  #2005/08/09 arakaki
                    $data["EXAMCOURSECD"][TEXT]  = substr($model->porgarr[$i][$divi],4,4);  #2005/08/09 arakaki
                    $data["JUDGEMENT"][TEXT]     = $model->pjudgearr[$i][$divi];            #2005/08/09 arakaki
                    $data["REGISTERCD"][TEXT]    = STAFFCD;
                    $data["UPDATED"][FUNC]       = "SYSDATE()";
                    $query = Query::insertSQL($data, $table);
                    $db->query($query);
                }
            }
        }
        return;
    }

    //名称マスタのをすべて取得
    function GetNamecd(&$db,$year,$cd1,$cd2)
    {
        static $namecd = array();
        
        if (!isset($namecd[$cd1])){
            $namecd[$cd1] = array();
            
            $query = " SELECT ";
            $query .= "     namecd1, ";
            $query .= "     namecd2 ";
            $query .= " FROM ";
            $query .= "     v_name_mst ";
            $query .= " WHERE ";
            $query .= "     year = '".$year."' AND ";
            $query .= "     namecd1 = '".$cd1."' ";
        
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $namecd[$row["NAMECD1"]][$row["NAMECD2"]] = $row["NAMECD2"];
            }
        }
        return $namecd[$cd1][$cd2];
    }
    //志望区分
    function GetDesirediv(&$db,$year,$testdiv,$desirediv)
    {
        static $row = array();
        
        if (!isset($row[$testdiv][$desirediv])){
            $query = " SELECT ";
            $query .= "     DESIREDIV, ";
            $query .= "     WISHNO, ";
            $query .= "     COURSECD, ";
            $query .= "     MAJORCD, ";
            $query .= "     EXAMCOURSECD ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_WISHDIV_MST ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '".$year."' AND ";
            $query .= "     TESTDIV = '".$testdiv."' AND ";
            $query .= "     DESIREDIV = '".$desirediv."' ";
            $query .= " ORDER BY ";
            $query .= "     DESIREDIV, ";
            $query .= "     WISHNO ";
            $row[$testdiv][$desirediv] = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        return $row[$testdiv][$desirediv];
    }
    //スピードアップ対応
    function GetOne(&$db,$query){
        static $wk = array();
        $md5 = md5($query);
        if (!isset($wk[$md5])){
            $wk[$md5] = $db->getOne($query);
        }
        return $wk[$md5];
    }
    //中学か高校かを判断
    function getSchoolName(&$db)
    {
        return $db->getOne("SELECT COUNT(*) FROM SCHOOL_MST WHERE SCHOOLNAME1 LIKE '%中学%'");
    }
} 
?>
