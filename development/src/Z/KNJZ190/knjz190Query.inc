<?php

require_once('for_php7.php');

class knjz190Query extends Query {

    //UPDATE処理
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $text_number = 0;

        for($grade=1; $grade<=3; $grade++)
        {
            for($hr_class=1; $hr_class<=$model->hr_class_dat[$grade]; $hr_class++)
            {
                for($semester=1; $semester<=$model->ctrl["学期数"]; $semester++)
                {
                    switch($semester){
                        case "1":
                        $data["CLASSWEEKS"][NUMBER] = $model->cw1[$text_number];
                        $data["CLASSDAYS"][NUMBER]  = $model->cd1[$text_number];
                        break;
                        case "2":
                        $data["CLASSWEEKS"][NUMBER] = $model->cw2[$text_number];
                        $data["CLASSDAYS"][NUMBER]  = $model->cd2[$text_number];
                        break;
                        case "3":
                        $data["CLASSWEEKS"][NUMBER] = $model->cw3[$text_number];
                        $data["CLASSDAYS"][NUMBER]  = $model->cd3[$text_number];
                        break;
                        case "4":
                        $data["CLASSWEEKS"][NUMBER] = $model->cw4[$text_number];
                        $data["CLASSDAYS"][NUMBER]  = $model->cd4[$text_number];
                        break;
                    }
                    $data["REGISTERCD"][TEXT]  = STAFFCD;
                    $data["UPDATED"][FUNC]  = "sysdate()";
                    $where  = " WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".$semester."'";
                    $where .= " AND GRADE = '".sprintf("%02d",$grade)."'";
                    $where .= " AND HR_CLASS = '".sprintf("%02d",$hr_class)."'";

                    $query = Query::updateSQL($data, "SCHREG_REGD_HDAT",$where);
                    $db->query($query);
                }
                $text_number++;
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }




    //list内データを取得
    function ReadQuery($model)
    {
        $query  = " SELECT DISTINCT GRADE || HR_CLASS AS GC_CL, GRADE, ";
        $query .= " HR_CLASS, HR_NAME, ";
        $query .= " SUM(CLASSWEEKS) AS CW0,";
        $query .= " SUM(CASE sc.SEMESTER WHEN '1' THEN CLASSWEEKS ELSE NULL END) AS CW1, ";
        $query .= " SUM(CASE sc.SEMESTER WHEN '2' THEN CLASSWEEKS ELSE NULL END) AS CW2, ";
        $query .= " SUM(CASE sc.SEMESTER WHEN '3' THEN CLASSWEEKS ELSE NULL END) AS CW3, ";
        $query .= " SUM(CASE sc.SEMESTER WHEN '4' THEN CLASSWEEKS ELSE NULL END) AS CW4, ";
        $query .= " SUM(CLASSDAYS) AS CD0,";
        $query .= " SUM(CASE sc.SEMESTER WHEN '1' THEN CLASSDAYS ELSE NULL END) AS CD1, ";
        $query .= " SUM(CASE sc.SEMESTER WHEN '2' THEN CLASSDAYS ELSE NULL END) AS CD2, ";
        $query .= " SUM(CASE sc.SEMESTER WHEN '3' THEN CLASSDAYS ELSE NULL END) AS CD3, ";
        $query .= " SUM(CASE sc.SEMESTER WHEN '4' THEN CLASSDAYS ELSE NULL END) AS CD4, ";
        $query .= " MAX(sc.UPDATED) ";
        $query .= " FROM SCHREG_REGD_HDAT sc,SEMESTER_MST se";
        $query .= " WHERE sc.YEAR   =  '";
        $query .= CTRL_YEAR ;
        $query .= "' ";
        $query .= " AND sc.SEMESTER = se.SEMESTER";
        $query .= " AND sc.YEAR = se.YEAR";
        $query .= " GROUP BY GRADE, HR_CLASS, HR_NAME";
        $query .= " ORDER BY 1,2 ";
        return $query;
    }

    //学期名を取得
    function SemesterGet($model)
    {
        $query  = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '";
        $query .= CTRL_YEAR ;
        $query .= "'";
        $query .= " ORDER BY SEMESTER";
        return $query;
    }

    //学年ごとのクラス数を取得
    function get_ClassData($year)
    {
        $query  = " SELECT COUNT(*) AS COUNT  FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE  YEAR = '";
        $query .= $year ;
        $query .= "' ";
        $query .= " and SEMESTER = (SELECT MIN(SEMESTER) FROM SCHREG_REGD_HDAT ) ";
        $query .= " GROUP BY GRADE ";
        $query .= " ORDER BY GRADE ";
        return $query;
    }
}
?>
