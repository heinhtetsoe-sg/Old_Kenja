<?php

require_once('for_php7.php');

class knjl115kQuery extends Query
{
    //名称
    function GetName($namecd1, $year)
    {
        $query = "
            SELECT 
                NAMECD1,NAMECD2, NAME1, NAME2 FROM V_NAME_MST
            WHERE
                YEAR    = '".$year."'
                AND NAMECD1 = '".$namecd1."'
            ORDER BY
                NAMECD2 ASC
                  ";
        return $query;
    }

    //合格者・不合格者最終番号取得
    function getNoticeNo($year)
    {
       $query = "
            WITH W_CHECK_NO AS (
               SELECT 
                    coalesce(MAX(SUCCESS_NOTICENO),'0') AS MAX_SUCCESS_NOTICENO,
                    coalesce(MAX(FAILURE_NOTICENO),'0') AS MAX_FAILURE_NOTICENO
               FROM
                   ENTEXAM_APPLICANTBASE_DAT
               WHERE
                   ENTEXAMYEAR='".$year."'
               UNION
               SELECT 
                    coalesce(MAX(OLD_SUCCESS_NOTICENO),'0') AS MAX_SUCCESS_NOTICENO,
                    coalesce(MAX(FAILURE_NOTICENO),'0')     AS MAX_FAILURE_NOTICENO
               FROM
                   ENTEXAM_APPLICANTBASE_DAT
               WHERE
                   ENTEXAMYEAR='".$year."'
            )
            SELECT 
                coalesce(MAX(MAX_SUCCESS_NOTICENO),'0') AS MAX_SUCCESS_NOTICENO,
                coalesce(MAX(MAX_FAILURE_NOTICENO),'0') AS MAX_FAILURE_NOTICENO
            FROM W_CHECK_NO
       ";

        return $query;
    }


#        if($mode == "success"){
#            $select = " coalesce(MAX(SUCCESS_NOTICENO),'0') AS MAX_SUCCESS_NOTICENO ";
#        }else{
#            $select = " coalesce(MAX(FAILURE_NOTICENO),'0') AS MAX_FAILURE_NOTICENO ";
#        }
# 
#        $query = "
#            SELECT 
#                ".$select."
#            FROM
#                ENTEXAM_APPLICANTBASE_DAT
#            WHERE
#                ENTEXAMYEAR='".$year."'
#        ";
# 
#        #echo $query;
#        return $query;
#    }

    //追加合格者回数取得
    function getJudgementGroupNo($year, $div)
    {
        $query = "
            SELECT 
                JUDGEMENT_GROUP_NO 
            FROM 
                ENTEXAM_APPLICANTBASE_DAT
            WHERE
                ENTEXAMYEAR='".$year."'
                AND TESTDIV='".$div."'
                AND JUDGEMENT_GROUP_NO IS NOT NULL
            GROUP BY 
                JUDGEMENT_GROUP_NO
        ";

        return $query;
    }


    //採番対象者抽出条件
    //  2006/02/09 一般合格者処理にスポーツ推薦者を追加
    function getTargetNumber(&$model)
    {
        $query = " WHERE  ";
        $query .= "    ENTEXAMYEAR  ='".$model->field["YEAR"]."' AND ";
        $query .= "    TESTDIV      ='".$model->field["TESTDIV"]."' AND ";
        if ($model->field["OUTPUT"] == '1'){         #合格者
            if ($model->field["SUC_RADIO"] == '11'){         #一般
                $query .= "     ((APPLICANTDIV ='0' AND JUDGEMENT IN('1','2','3','9')) OR (APPLICANTDIV ='3' AND JUDGEMENT ='4' ))";
            }else if ($model->field["SUC_RADIO"] == '12'){   #追加繰上
                $query .= "     JUDGEMENT IN('5','6') ";
                if($model->field["PASSCOUNT"] != ""){
                    $query.= " AND JUDGEMENT_GROUP_NO ='".$model->field["PASSCOUNT"]."' ";
                }
            }else if ($model->field["SUC_RADIO"] == '13'){   #附属推薦
                $query .= "     (APPLICANTDIV ='1' OR APPLICANTDIV ='2') AND ";
                $query .= "     JUDGEMENT ='4' ";
            }

        }else if ($model->field["OUTPUT"] == '2'){   #不合格者
            $query .= "     JUDGEMENT ='7' ";
        }
        //TODO:インフルエンザ対応
        $query .= "     AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') ";
        return $query;
    }

    //採番番号重複チェック
    //  合格から不合格、不合格から合格になった場合、通知番号は残る、残った通知番号も重複チェック対象とする。
    //  追加繰上合格者は、旧番号、新番号両方がNotNullで採番済とみなす。
    #   2006/01/12 旧番号も重複チェック対象とする。上記、追加繰上合格者の条件削除。
    function getDuplicationCheck(&$model)
    {
        $query = "
            WITH W_CHECK_NO AS (
                SELECT SUCCESS_NOTICENO  AS CHECK_NO
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$model->field["YEAR"]."'
                   AND SUCCESS_NOTICENO IS NOT NULL
                UNION
                SELECT OLD_SUCCESS_NOTICENO  AS CHECK_NO
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$model->field["YEAR"]."'
                   AND OLD_SUCCESS_NOTICENO IS NOT NULL
                UNION
                SELECT FAILURE_NOTICENO AS CHECK_NO
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$model->field["YEAR"]."'
                   AND FAILURE_NOTICENO IS NOT NULL
            )
            SELECT COUNT(*) AS COUNT FROM W_CHECK_NO
               WHERE DEC(CHECK_NO) BETWEEN ".$model->field["STARTNUMBER"]." AND ".$model->endNumber."
        ";

       #echo $query;
       return $query;
    }

    //終了番号取得処理
    function setEndNumber(&$model)
    {
        $db = Query::dbCheckOut();
        $sql  = " SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTBASE_DAT " ;
        $sql .= knjl115kQuery::getTargetNumber($model);
        $Row = $db->getRow($sql,DB_FETCHMODE_ASSOC);

        //終了番号
        $endNumber = $model->field["STARTNUMBER"] + ( $Row["COUNT"] -1 );
        Query::dbCheckIn($db);

        if($endNumber < $model->field["STARTNUMBER"]){
            $model->endNumber = $model->field["STARTNUMBER"];
        }else{
            $model->endNumber = $endNumber;
        }
        return true;
    }

    //番号採番更新処理
    function updateChainNumber(&$model)
    {
        $notice = array ( "1" => "SUCCESS_NOTICENO",
                          "2" => "FAILURE_NOTICENO" );

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //採番対象抽出条件
        $where = knjl115kQuery::getTargetNumber($model);

        //採番対象者通知Noクリア    #2006/01/13クリア処理不要
#        $query = " UPDATE ";
#        $query .= "     ENTEXAM_APPLICANTBASE_DAT AS T1 ";
#        $query .= " SET ";
#        if ($model->field["SUC_RADIO"] == '11'){   #一般
#            $query .= "OLD_SUCCESS_NOTICENO = Null, ";
#        }
#        if ($model->field["SUC_RADIO"] == '12'){   #追加繰上
#            $query .= "OLD_SUCCESS_NOTICENO = SUCCESS_NOTICENO, ";
#        }
#        $query .= $notice[$model->field["OUTPUT"]]." = NULL ";
#        $query .= $where;
#        if ($model->field["SUC_RADIO"] == '12'){   #追加繰上
#            $query .= " AND (OLD_SUCCESS_NOTICENO IS NULL OR OLD_SUCCESS_NOTICENO = '') ";
#        }
#        $db->query($query);


        //追加繰上 旧番号保管処理 2006/01/06
        #2006/01/13 6:繰上合格者のみ対象とする。
        if ($model->field["SUC_RADIO"] == '12'){   #追加繰上
            $query = "";
            $query = "
                UPDATE ENTEXAM_APPLICANTBASE_DAT AS T1
                   SET OLD_SUCCESS_NOTICENO=SUCCESS_NOTICENO,
                       SUCCESS_NOTICENO=Null
               ".$where."

                     AND JUDGEMENT = '6'
                     AND OLD_SUCCESS_NOTICENO IS NULL";

             #echo "<BR><BR>".$query."<BR><BR>";
             $db->query($query);
         }

        //番号更新
        $query = "";
        $query = "
            UPDATE ENTEXAM_APPLICANTBASE_DAT AS T1
            SET(".$notice[$model->field["OUTPUT"]].") = 

                (SELECT CHAR(ST1.NUM) FROM
                    (SELECT
                        (ROW_NUMBER() OVER(ORDER BY EXAMNO) + ".$model->field["STARTNUMBER"]." - 1) AS NUM, EXAMNO,JUDGEMENT_GROUP_NO
                     FROM
                        ENTEXAM_APPLICANTBASE_DAT
                  ".$where."
                    ) AS ST1 
                WHERE
                    T1.EXAMNO = ST1.EXAMNO
                ORDER BY
                    EXAMNO
                )
            ,REGISTERCD = '".STAFFCD."'
            ,UPDATED = SYSDATE()
            ".$where." ";
        #echo "<BR><BR>".$query."<BR><BR>";
        $db->query($query);
        $db->commit(); 
        Query::dbCheckIn($db);

        return true;
    }

    #番号クリア処理 合格者 一般 2005/12/29
    #  2006/02/09 一般合格者処理にスポーツ推薦者を追加
    function ClearNumber0111(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query = "
            UPDATE ENTEXAM_APPLICANTBASE_DAT
               SET SUCCESS_NOTICENO = Null,
                   OLD_SUCCESS_NOTICENO=Null
                   ,REGISTERCD = '".STAFFCD."'
                   ,UPDATED = SYSDATE()
             WHERE ENTEXAMYEAR='".$model->field["YEAR"]."'
               AND TESTDIV='".$model->field["TESTDIV"]."'
               AND ((APPLICANTDIV ='0' AND JUDGEMENT IN('1','2','3','9')) OR (APPLICANTDIV ='3' AND JUDGEMENT ='4' ))
               AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1')
        ";
        $db->query($query);
        $db->commit(); 
        Query::dbCheckIn($db);
        return true;
    }

    #番号クリア処理 合格者 追加繰上 2005/12/29
    #2006/01/12 条件追加(AND OLD_SUCCESS_NOTICENO IS NOT NULL)
    #2006/01/13 5:追加合格者は、旧番号との関連なしへ変更
    function ClearNumber0112(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query = "
            UPDATE ENTEXAM_APPLICANTBASE_DAT
               SET SUCCESS_NOTICENO     = Null
                   ,REGISTERCD = '".STAFFCD."'
                   ,UPDATED = SYSDATE()
             WHERE ENTEXAMYEAR='".$model->field["YEAR"]."'
               AND TESTDIV='".$model->field["TESTDIV"]."'
               AND (JUDGEMENT = '5' 
                OR (JUDGEMENT = '6' AND OLD_SUCCESS_NOTICENO IS NOT NULL))
               AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1')
        ";
        if($model->field["PASSCOUNT"] != ""){
            $query.= " AND JUDGEMENT_GROUP_NO ='".$model->field["PASSCOUNT"]."' ";
        }
        # echo $query;
        $db->query($query);
        $db->commit(); 
        Query::dbCheckIn($db);
        return true;
    }

    #番号クリア処理 付属推薦 2006/01/04
    function ClearNumber0113(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query = "
            UPDATE ENTEXAM_APPLICANTBASE_DAT
               SET SUCCESS_NOTICENO = Null
                   ,REGISTERCD = '".STAFFCD."'
                   ,UPDATED = SYSDATE()
             WHERE ENTEXAMYEAR='".$model->field["YEAR"]."'
               AND TESTDIV='".$model->field["TESTDIV"]."'
               AND (APPLICANTDIV ='1' OR APPLICANTDIV ='2')
               AND JUDGEMENT = '4' 
               AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1')
        ";
        $db->query($query);
        $db->commit(); 
        Query::dbCheckIn($db);
        return true;
    }

    #番号クリア処理 不合格者 2005/12/29
    function ClearNumber0200(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query = "
            UPDATE ENTEXAM_APPLICANTBASE_DAT
               SET FAILURE_NOTICENO = Null
                   ,REGISTERCD = '".STAFFCD."'
                   ,UPDATED = SYSDATE()
             WHERE ENTEXAMYEAR='".$model->field["YEAR"]."'
               AND TESTDIV='".$model->field["TESTDIV"]."'
               AND JUDGEMENT ='7'
               AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1')
        ";
        $db->query($query);
        $db->commit(); 
        Query::dbCheckIn($db);
        return true;
    }


    #一覧データ取得 2005/12/29
    #2006/01/13 5:追加合格者は、旧番号との関連なしへ変更
    #  2006/02/09 一般合格者処理にスポーツ推薦者を追加
    function GetListDT($year, $title3)
    {
        $query = "
            SELECT * FROM
            (
                SELECT MIN(SUCCESS_NOTICENO)            AS S_NO,
                       MAX(SUCCESS_NOTICENO)            AS E_NO,
                       COUNT(*)                         AS PERSONS,
                       TESTDIV                          AS TESTDIV,
                       '0'                              AS SPECIAL_REASON_DIV,
                       MEISYOU_GET(TESTDIV,'L003',1)    AS TESTDIV_NAME,
                      '0111'                            AS ID_KEY,
                      '合格者　一般'                    AS DIVISION,
                      '00'                              AS JUDGEMENT_GROUP_NO,
                       MAX(UPDATED)                     AS TRNCEDATE
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$year."'
                   AND ((APPLICANTDIV ='0' AND JUDGEMENT IN('1','2','3','9')) OR (APPLICANTDIV ='3' AND JUDGEMENT ='4' ))
                   AND SPECIAL_REASON_DIV = '0' 
                 GROUP BY TESTDIV
                UNION
                SELECT MIN(CASE WHEN JUDGEMENT = '6' AND OLD_SUCCESS_NOTICENO IS NULL THEN NULL ELSE SUCCESS_NOTICENO END) AS S_NO,
                       MAX(CASE WHEN JUDGEMENT = '6' AND OLD_SUCCESS_NOTICENO IS NULL THEN NULL ELSE SUCCESS_NOTICENO END) AS E_NO,
                       COUNT(*)                         AS PERSONS,
                       TESTDIV                          AS TESTDIV,
                       '0'                              AS SPECIAL_REASON_DIV,
                       MEISYOU_GET(TESTDIV,'L003',1)    AS TESTDIV_NAME,
                      '0112' || COALESCE(JUDGEMENT_GROUP_NO, '0')      AS ID_KEY,
                      '合格者　追加繰上 ' || COALESCE(JUDGEMENT_GROUP_NO, '　') || '回目' AS DIVISION,
                       JUDGEMENT_GROUP_NO               AS JUDGEMENT_GROUP_NO,
                       MAX(UPDATED)                     AS TRNCEDATE
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$year."'
                   AND JUDGEMENT IN('5','6') 
                   AND SPECIAL_REASON_DIV = '0' 
                 GROUP BY TESTDIV,JUDGEMENT_GROUP_NO
                UNION
                SELECT MIN(SUCCESS_NOTICENO)            AS S_NO,
                       MAX(SUCCESS_NOTICENO)            AS E_NO,
                       COUNT(*)                         AS PERSONS,
                       TESTDIV                          AS TESTDIV,
                       '0'                              AS SPECIAL_REASON_DIV,
                       MEISYOU_GET(TESTDIV,'L003',1)    AS TESTDIV_NAME,
                      '0113'                            AS ID_KEY,
                      '合格者　".$title3."'             AS DIVISION,
                      '00'                              AS JUDGEMENT_GROUP_NO,
                       MAX(UPDATED)                     AS TRNCEDATE
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$year."'
                   AND (APPLICANTDIV ='1' OR APPLICANTDIV ='2')
                   AND JUDGEMENT ='4'
                   AND SPECIAL_REASON_DIV = '0' 
                 GROUP BY TESTDIV
                UNION
                SELECT MIN(FAILURE_NOTICENO)            AS S_NO,
                       MAX(FAILURE_NOTICENO)            AS E_NO,
                       COUNT(*)                         AS PERSONS,
                       TESTDIV                          AS TESTDIV,
                       '0'                              AS SPECIAL_REASON_DIV,
                       MEISYOU_GET(TESTDIV,'L003',1)    AS TESTDIV_NAME,
                      '0200'                            AS ID_KEY,
                      '不合格者'                        AS DIVISION,
                      '00'                              AS JUDGEMENT_GROUP_NO,
                       MAX(UPDATED)                     AS TRNCEDATE
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$year."'
                   AND JUDGEMENT ='7'
                   AND SPECIAL_REASON_DIV = '0' 
                 GROUP BY TESTDIV
                UNION
                SELECT MIN(SUCCESS_NOTICENO)            AS S_NO,
                       MAX(SUCCESS_NOTICENO)            AS E_NO,
                       COUNT(*)                         AS PERSONS,
                       TESTDIV                          AS TESTDIV,
                       '1'                              AS SPECIAL_REASON_DIV,
                       '追試験'                         AS TESTDIV_NAME,
                      '0111'                            AS ID_KEY,
                      '合格者　一般'                    AS DIVISION,
                      '00'                              AS JUDGEMENT_GROUP_NO,
                       MAX(UPDATED)                     AS TRNCEDATE
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$year."'
                   AND ((APPLICANTDIV ='0' AND JUDGEMENT IN('1','2','3','9')) OR (APPLICANTDIV ='3' AND JUDGEMENT ='4' ))
                   AND SPECIAL_REASON_DIV = '1' 
                 GROUP BY TESTDIV
                UNION
                SELECT MIN(CASE WHEN JUDGEMENT = '6' AND OLD_SUCCESS_NOTICENO IS NULL THEN NULL ELSE SUCCESS_NOTICENO END) AS S_NO,
                       MAX(CASE WHEN JUDGEMENT = '6' AND OLD_SUCCESS_NOTICENO IS NULL THEN NULL ELSE SUCCESS_NOTICENO END) AS E_NO,
                       COUNT(*)                         AS PERSONS,
                       TESTDIV                          AS TESTDIV,
                       '1'                              AS SPECIAL_REASON_DIV,
                       '追試験'                         AS TESTDIV_NAME,
                      '0112' || COALESCE(JUDGEMENT_GROUP_NO, '0')      AS ID_KEY,
                      '合格者　追加繰上 ' || COALESCE(JUDGEMENT_GROUP_NO, '　') || '回目' AS DIVISION,
                       JUDGEMENT_GROUP_NO               AS JUDGEMENT_GROUP_NO,
                       MAX(UPDATED)                     AS TRNCEDATE
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$year."'
                   AND JUDGEMENT IN('5','6') 
                   AND SPECIAL_REASON_DIV = '1' 
                 GROUP BY TESTDIV,JUDGEMENT_GROUP_NO
                UNION
                SELECT MIN(SUCCESS_NOTICENO)            AS S_NO,
                       MAX(SUCCESS_NOTICENO)            AS E_NO,
                       COUNT(*)                         AS PERSONS,
                       TESTDIV                          AS TESTDIV,
                       '1'                              AS SPECIAL_REASON_DIV,
                       '追試験'                         AS TESTDIV_NAME,
                      '0113'                            AS ID_KEY,
                      '合格者　".$title3."'             AS DIVISION,
                      '00'                              AS JUDGEMENT_GROUP_NO,
                       MAX(UPDATED)                     AS TRNCEDATE
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$year."'
                   AND (APPLICANTDIV ='1' OR APPLICANTDIV ='2')
                   AND JUDGEMENT ='4'
                   AND SPECIAL_REASON_DIV = '1' 
                 GROUP BY TESTDIV
                UNION
                SELECT MIN(FAILURE_NOTICENO)            AS S_NO,
                       MAX(FAILURE_NOTICENO)            AS E_NO,
                       COUNT(*)                         AS PERSONS,
                       TESTDIV                          AS TESTDIV,
                       '1'                              AS SPECIAL_REASON_DIV,
                       '追試験'                         AS TESTDIV_NAME,
                      '0200'                            AS ID_KEY,
                      '不合格者'                        AS DIVISION,
                      '00'                              AS JUDGEMENT_GROUP_NO,
                       MAX(UPDATED)                     AS TRNCEDATE
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR='".$year."'
                   AND JUDGEMENT ='7'
                   AND SPECIAL_REASON_DIV = '1' 
                 GROUP BY TESTDIV
                ) AS T01
            ORDER BY SPECIAL_REASON_DIV,TESTDIV,ID_KEY
        ";
        #echo $query;
        return $query;
    }

    //追加繰上合格者数取得
    function GetCountAdvance($year, $div){
        $query = "
                SELECT COUNT(*) AS COUNT
                  FROM ENTEXAM_APPLICANTBASE_DAT
                 WHERE ENTEXAMYEAR ='".$year."'
                   AND TESTDIV     ='".$div."'
                   AND JUDGEMENT IN('5','6') 
                   AND SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1')
        ";
        #echo $query;
        return $query;
    }

    //中学・高校判定 2005/12/29
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%中学%' ";

        return $query;
    }
}
?>