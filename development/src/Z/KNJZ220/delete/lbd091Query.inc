<?php

require_once('for_php7.php');

class lbd091Query extends Query {

    function combo_grdQuery()
    {
        return " SELECT GRADE || ' ・・ｽｯ' AS SHOWGRADE, GRADE FROM SCHREG_REGD_HDAT GROUP BY GRADE";
    }
    
    function comboQuery()
    {
        return " SELECT CLASSCD,CLASSNAME FROM CLASS_MST where integer(classcd) < 50";
    }
    
    function Copy_comboQuery($code,$gd)
    {        
        $query  = " SELECT DISTINCT ";
        $query .= " R.GRADE, R.ASSESSCD, S.SUBCLASSCD, S.SUBCLASSNAME ";
        $query .= " FROM RELATIVEASSESS_MST R LEFT JOIN SUBCLASS_MST S ON R.SUBCLASSCD = S.SUBCLASSCD ";
        $query .= " WHERE R.ASSESSCD = '";
        $query .= $code;
        $query .= "' ";
        $query .= " AND R.GRADE = '";
        $query .= $gd;
        $query .= "' order by subclasscd";
        return $query;
    }

    function ReadQuery($cd)
    { 
        $query  = "SELECT ";
        $query .= " * FROM SUBCLASS_MST ";
        $query .= " WHERE substr(SUBCLASSCD,1,2) = '";
        $query .= $cd ;
        $query .= "' ORDER BY SUBCLASSCD";
        return $query;
    }

    function selectQuery($model)
    {
        $sep = $model->sepa; 

        if($model->copy_flg == true) {
            $sbclss = $model->copy_subclasscd;
            $grd    = $model->copy_grade;
        } else if($model->copy_flg == false){
            $sbclss = $model->field1["SUBCLASSCD"];
            $grd    = $model->grade;
        }
        $query  =  " SELECT * FROM RELATIVEASSESS_MST ";
        $query .=  " WHERE ASSESSCD = '";
        $query .=  $sep ;
        $query .=  "' ";
        $query .=  " AND SUBCLASSCD = '";
        $query .=  $sbclss;
        $query .=  "' ";
        $query .=  " AND GRADE = '";
        $query .=  $grd;
        $query .=  "' ";
        $query .= " ORDER BY ASSESSLOW";
        return $query;
    }

    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  =  " DELETE FROM RELATIVEASSESS_MST ";
        $query .=  " WHERE ASSESSCD = '";
        $query .=  $model->sepa ;
        $query .=  "' ";
        $query .=  " AND SUBCLASSCD = '";
        $query .=  $model->field1["SUBCLASSCD"] ;
        $query .=  "' ";
        $query .=  " AND GRADE = '";
        $query .=  $model->grade ;
        $query .=  "' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }

    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();        
        $ii = ($model->sepa == 1) ? 4 : 9;
        
        for($i=0; $i<=$ii; $i++)
        {
            $ASSESSLOW  = "ASSESSLOW".($i + 1);
                        
            $data["GRADE"][TEXT]                 = $model->grade;
            $data["SUBCLASSCD"][TEXT]                 = $model->field1["SUBCLASSCD"];
            $data["ASSESSCD"][TEXT]                 = $model->sepa;
            $data["ASSESSLEVEL"][NUMBER]              = ($i + 1);
            if($i == 0){
                $data["ASSESSLOW"][NUMBER]                = 0;
            }else{
                $data["ASSESSLOW"][NUMBER]                = $model->field2[$ASSESSLOW];            
            }
            
            if($model->sepa == 1){
                if($i == 4){
                    $data["ASSESSHIGH"][NUMBER]                = 100;
                }else{
                    $ASSESSHIGH = "ASSESSLOW".($i + 2);
                    $data["ASSESSHIGH"][NUMBER] = ((int)$model->field2[$ASSESSHIGH] - 1);
                    ((int)$model->field2[$ASSESSHIGH] - 1);
                }
            }else{
                if($i == 9){
                    $data["ASSESSHIGH"][NUMBER]                = 100;
                }else{
                    $ASSESSHIGH = "ASSESSLOW".($i + 2);
                    $data["ASSESSHIGH"][NUMBER]                = ((int)$model->field2[$ASSESSHIGH] - 1);
                }
            }
            $data["UPDATED"][FUNC]                = "sysdate()";
            $query = Query::insertSQL($data, "relativeassess_mst");
            $db->query($query);
        }
        Query::dbCheckIn($db);    
        return true;
    }
}
?>
