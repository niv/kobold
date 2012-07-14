m = []
IO.readlines(ARGV.shift).each do |ln|
  if ln =~ /^\s*public native static synchronized (\S+) ([^\(]+)\((.*)\) throws .*;$/
    args = $3.split(", ").map {|x| x.split(" ").reverse }
    m << [$1, $2, args]
  end
end

puts "package es.elv.kobold

import org.nwnx.nwnx2.jvm._

private[kobold] trait NWScriptProxyGen {
  protected def wrap[R](method: String)(c: => R): R

"
m.each {|f|
  
  f[2].map {|pp|
    pp[1][0..0] = pp[1][0..0].upcase
  }

  if (f[2].size > 0)
    puts "  def %s(%s) = wrap(\"%s\") { NWScript.%s(%s) } " % [ f[1],
      f[2].map {|p| p.join(": ")}.join(", "),
      f[1], f[1],
      f[2].map {|p| p[0] }.join(", ")
    ]
  else
    puts "  def %s = wrap(\"%s\") { NWScript.%s } " % [ f[1], f[1], f[1] ]
  end
}
puts "}"
