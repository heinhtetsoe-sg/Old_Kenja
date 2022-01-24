<?php

require_once('for_php7.php');

class knjx150query extends Query {


    //年度一覧
	function getSelectFieldSQL()
	{
		$query  = "select distinct t1.year,t1.semester,t2.semestername ";
		$query .= "from schreg_regd_hdat t1 ";
		$query .= "		inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
		$query .= "order by t1.year,t1.semester";

		return $query;	
	}

    //年組一覧
	function getSelectFieldSQL2($model)
	{
		$query  = "select distinct grade,hr_class,hr_name ";
		$query .= "from schreg_regd_hdat ";
		$query .= "where YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
		$query .= "order by grade,hr_class";

		return $query;	
	}

    //データ出力
	function getselectDataSQL($model)
	{
		$query  = "select YEAR,SEMESTER,SCHREGNO,COMMUNICATION,REGISTERCD from hreportremark_dat ";
		$query .= "where YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
		$query .= "  and SCHREGNO in ( ";

		$query .= "select schregno from schreg_regd_dat ";
		$query .= "where YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
		if( $model->field["GRADE_HR_CLASS"]!="" ) 
			$query .= "and GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";

		$query .= "  ) ";
		$query .= "order by schregno";

		return $query;	
	}

    //マスタ(存在チェック用)
    function getMasterCheck($data, $chk_no)
    {
		$flg 	= true;
		$query 	= "";
		//条件選択
		switch ($chk_no) 
		{
			case "1":
				$where = "schreg_base_mst where SCHREGNO = '".$data["SCHREGNO"]."' ";
				break;
			case "2":
				$where  = "schreg_regd_dat ";
				$where .= "where 	SCHREGNO 	= '".$data["SCHREGNO"]."' ";
				$where .= "		and YEAR 		= '".$data["YEAR"]."' ";
				$where .= "		and SEMESTER 	= '".$data["SEMESTER"]."' ";
				break;
			default:
				$flg = false;
				break;
		}
		if($flg) $query = "SELECT 'X' FROM " . $where;

        return $query;
    }


    //マスタ(追加・更新・削除用)
	function getExist_Check($data)
	{
		$query  = "select 'X' from hreportremark_dat ";
		$query .= "where SCHREGNO = '".$data["SCHREGNO"]."' ";
		$query .= "and YEAR = '".$data["YEAR"]."' ";
		$query .= "and SEMESTER = '".$data["SEMESTER"]."' ";

		return $query;	
	}


    //マスタデータの削除
    function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM hreportremark_dat ";
		$query .= "where SCHREGNO = '".$data["SCHREGNO"]."' ";
		$query .= "and YEAR = '".$data["YEAR"]."' ";
		$query .= "and SEMESTER = '".$data["SEMESTER"]."' ";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv(&$db, $data, $registercd)
    {

			$data1["YEAR"][TEXT] 				= $data["YEAR"];
			$data1["SEMESTER"][TEXT] 			= $data["SEMESTER"];
			$data1["SCHREGNO"][TEXT] 			= $data["SCHREGNO"];
			$data1["COMMUNICATION"][TEXT] 		= $data["COMMUNICATION"];
//			$data1["COMMUNICATION"][TEXT] 		= str_replace("rn","\r\n",$data["COMMUNICATION"]);//'rn'を改行マークに置換する。

			$data1["REGISTERCD"][TEXT] 			= $registercd;
			$data1["UPDATED"][NUMBER] 			= "sysdate()";

			$query = Query::insertSQL($data1, "hreportremark_dat");

            $result = $db->query($query);

    }


    //マスタの更新（ＣＳＶデータより読込）
    function updateQueryCsv(&$db, $data, $registercd)
    {

			$data1["YEAR"][TEXT] 				= $data["YEAR"];
			$data1["SEMESTER"][TEXT] 			= $data["SEMESTER"];
			$data1["SCHREGNO"][TEXT] 			= $data["SCHREGNO"];
			$data1["COMMUNICATION"][TEXT] 		= $data["COMMUNICATION"];
//			$data1["COMMUNICATION"][TEXT] 		= str_replace("rn","\r\n",$data["COMMUNICATION"]);//'rn'を改行マークに置換する。

			$data1["REGISTERCD"][TEXT] 			= $registercd;
			$data1["UPDATED"][NUMBER] 			= "sysdate()";

			$where  = "where SCHREGNO = '".$data["SCHREGNO"]."' ";
			$where .= "and YEAR = '".$data["YEAR"]."' ";
			$where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
			$query = Query::updateSQL($data1, "hreportremark_dat", $where);

            $result = $db->query($query);

    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
?>
