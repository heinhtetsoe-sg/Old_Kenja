<?php

require_once('for_php7.php');

class knjl210rquery extends Query {

    //名称マスタより取得
    function getNameMst($model, $year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        if ($namecd1 == 'L004') {
            $query .= "     NAMECD2 || ':' || ABBV2 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE,";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMECD2 = '".$model->fixApplicantDiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //取込
    function InsertQuery($model,&$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        for($i=0; $i<$count; $i++)
        {
        
            if ($this->field["SHORI_MEI"] == "1" && 0 == $db->getOne(knjl210rQuery::dataCntSql($this, $data_arr[$i]["BEFORE_PAGE"], $data_arr[$i]["BEFORE_SEQ"]))) {
                $data = array();

                $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]         = $model->field["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]              = $model->field["TESTDIV"];
                $data["BEFORE_PAGE"][TEXT]          = $data_arr[$i]["BEFORE_PAGE"];
                $data["BEFORE_SEQ"][TEXT]           = $data_arr[$i]["BEFORE_SEQ"];
                
                //内諾コースを受験コースマスタから取得
                //全角文字に変換
                $henkan_kigou = mb_convert_kana($data_arr[$i]["NAIDAKU_KIGOU"],R,"utf-8");
                $couse_array = array();
                $couse_array = knjl210rQuery::getCourse($model, $henkan_kigou, $db);
                
                $data["BEFORE_COURSECD"][TEXT]      = $couse_array["COURSECD"];
                $data["BEFORE_MAJORCD"][TEXT]       = $couse_array["MAJORCD"];
                $data["BEFORE_EXAMCOURSECD"][TEXT]  = $couse_array["EXAMCOURSECD"];
                $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
                $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
                //半角を全角変換
                $set_name_kana = "";
                $set_name_kana = mb_convert_kana($data_arr[$i]["NAME_KANA"], "K", "UTF-8");
                $data["NAME_KANA"][TEXT]            = $set_name_kana;
                $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
                if ($model->field["TESTDIV"] === '2') {
                    $data["NAISIN1"][NUMBER]            = $data_arr[$i]["NAISIN1"];
                }
                $data["NAISIN2"][NUMBER]            = $data_arr[$i]["NAISIN2"];
                $data["NAISIN3"][NUMBER]            = $data_arr[$i]["NAISIN3"];
                $data["ATTEND1"][NUMBER]            = $data_arr[$i]["ATTEND1"];
                $data["ATTEND2"][NUMBER]            = $data_arr[$i]["ATTEND2"];
                $data["ATTEND3"][NUMBER]            = $data_arr[$i]["ATTEND3"];
                $data["ATTEND_TOTAL"][NUMBER]       = $data_arr[$i]["ATTEND_TOTAL"];
                if ($model->field["TESTDIV"] === '2') {
                    $data["SENBATU1_SCHOOL"][TEXT]      = $data_arr[$i]["SENBATU1_SCHOOL"];
                    $data["SENBATU1_MAJOR"][TEXT]       = $data_arr[$i]["SENBATU1_MAJOR"];
                    $data["SENBATU2_SCHOOL"][TEXT]      = $data_arr[$i]["SENBATU2_SCHOOL"];
                    $data["SENBATU2_MAJOR"][TEXT]       = $data_arr[$i]["SENBATU2_MAJOR"];
                }
                $data["NANKAN_FLG"][TEXT]           = $data_arr[$i]["NANKAN_FLG"];
                $data["SCHOLARSHIP"][TEXT]          = $data_arr[$i]["SCHOLARSHIP"];
                if ($model->field["TESTDIV"] === '1') {
                    $data["RECOM_FLG"][TEXT]            = $data_arr[$i]["RECOM_FLG"];
                    $data["RECOM_REMARK"][TEXT]         = $data_arr[$i]["RECOM_REMARK"];
                }
                $data["REMARK"][TEXT]               = $data_arr[$i]["REMARK"];
                
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = "SYSDATE()";

                $query = Query::insertSQL($data, "ENTEXAM_APPLICANT_BEFORE_DAT");
                $db->query($query);

                $cnt++;
            } else {
                $check_error = '※CSVデータ内で重複(事前番号-貢/連番)';
                if (strlen($check_error) > 120) {
                    $check_error = mb_strcut($check_error,0,115);
                    $check_error .= "..他";
                }
                if (0 == $db->getOne(knjl210rQuery::CsvErrCountQuery($data_arr[$i]["LINE"]))) {
                    knjl210rQuery::insertQueryErr($db, $data_arr[$i]["LINE"], $check_error);
                }
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除
    function DeleteQuery($model,&$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        for($i=0; $i<$count; $i++)
        {
            $query  = " DELETE FROM ENTEXAM_APPLICANT_BEFORE_DAT ";
            $query .= " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
            $query .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "   AND TESTDIV       = '".$model->field["TESTDIV"]."' ";
            $query .= "   AND BEFORE_PAGE   = '".$data_arr[$i]["BEFORE_PAGE"]."' ";
            $query .= "   AND BEFORE_SEQ    = '".$data_arr[$i]["BEFORE_SEQ"]."' ";
            
            $db->query($query);
            
            //削除処理を実行する際、志願者詳細データのSEQ='002'のデータがある場合は志願者詳細データを削除する
            $detail_examno = knjl210rQuery::getDetailDat($model, $data_arr[$i]["BEFORE_PAGE"], $data_arr[$i]["BEFORE_SEQ"], $db);
            if ($detail_examno) {
                $query  = " DELETE FROM ";
                $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
                $query .= " WHERE ";
                $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."' ";
                $query .= " AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
                $query .= " AND EXAMNO       = '".$detail_examno."' ";
                $query .= " AND SEQ          = '002'";
                
                $db->query($query);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //事前データを削除する時、志願者詳細データに存在するかを確認するため受験番号を取得
    function getDetailDat($model, $page, $seq, $db)
    {
        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= " AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " AND SEQ = '002' ";
        $query .= " AND REMARK1 = '".$page."' ";
        $query .= " AND REMARK2 = '".$seq."' ";
        
        $examno = $db->getOne($query);

        return $examno;
    }

    //内諾記号より受験コース取得
    function getCourse($model, $naidaku_kigou, $db)
    {
        $course_array = array();

        $query  = " SELECT DISTINCT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' ";
        $query .= " AND APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " AND TESTDIV         = '".$model->field["TESTDIV"]."' ";
        $query .= " AND EXAMCOURSE_MARK = '".$naidaku_kigou."' ";

        $course_array = $db->getRow($query, DB_FETCHMODE_ASSOC);

        return $course_array;
    }

    //内諾記号より受験コースデータがあるか確認
    function countCourse($model, $naidaku_kigou)
    {
        $course_array = array();
        
        $query  = " SELECT  ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->ObjYear."' ";
        $query .= " AND APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " AND TESTDIV         = '".$model->field["TESTDIV"]."' ";
        $query .= " AND EXAMCOURSE_MARK = '".$naidaku_kigou."' ";

        return $query;
    }

    //中学校コード存在チェックSQL
    function dataCntSchoolSql($model, $fs_cd)
    {
        $query  = " SELECT  ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM  ";
        $query .= "     FINSCHOOL_MST  ";
        $query .= " WHERE FINSCHOOLCD   = '".$fs_cd."' ";
                        
        return $query;
    }

    //重複チェックSQL
    function dataCntSql($model, $before_page, $before_seq)
    {
        $query  = " SELECT  ";
        $query .= "     COUNT(*)  ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT  ";
        $query .= " WHERE ENTEXAMYEAR   = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND TESTDIV       = '".$model->field["TESTDIV"]."' ";
        $query .= "   AND BEFORE_PAGE   = '".$before_page."' ";
        $query .= "   AND BEFORE_SEQ    = '".$before_seq."' ";

        return $query;
    }

    //名称マスタよりヘッダーフォーマット用の名称取得
    function getFormatName($model)
    {
        $query  = " SELECT ";
        $query .= "     ABBV2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' ";
        $query .= " AND NAMECD1 = 'L004' ";
        $query .= " AND NAMECD2 = '".$model->field["TESTDIV"]."' ";

        return $query;
    }

    //Err用CVS作成用
    function selectCsvErrQuery()
    {
        $query  = "SELECT MSGROW, MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL210R'";

        return $query;
    }

    //エラーデータの削除
    function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL210R'";
        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = "KNJL210R";
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);

    }
    
    //エラーDBカウント
    function csvErrCountQuery($line)
    {
    
        $query  = "SELECT COUNT(*) AS CNT FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = 'KNJL210R' AND MSGROW = ".$line." ";

        return $query;
    }
}
?>
